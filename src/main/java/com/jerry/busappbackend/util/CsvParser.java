package com.jerry.busappbackend.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jerry.busappbackend.entity.BusRecordEntity;
import com.jerry.busappbackend.exception.CsvParsingException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;

@Component
public class CsvParser {

    private static final Logger logger = LogManager.getLogger(CsvParser.class);

    private Path dir;
    private Path finalDataPath;
    private Path tempDataPath;
    private final int NUM_ROWS = 100000;
    private Path rawDataPath;

    public CsvParser(@Value("${app.data.path}") String rawDataPathString) {
        if (rawDataPathString == null || rawDataPathString.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw data file path must be provided.");
        }

        Path rawDataPath = Paths.get(rawDataPathString);
        this.dir = rawDataPath.getParent();

        if (!Files.exists(rawDataPath)) {
            throw new IllegalArgumentException("Raw data file does not exist at the provided path: " + rawDataPathString);
        }


        this.rawDataPath = Paths.get(rawDataPathString);
        
        String filenameWithExtension = rawDataPath.getFileName().toString();
        String filename = filenameWithExtension.replaceFirst("[.][^.]+$", "");
        this.finalDataPath = this.dir == null ? Paths.get(filename + "_clean.csv") : this.dir.resolve(filename + "_clean.csv");  
        this.tempDataPath = this.dir == null ? Paths.get(filename + "_temp.csv") : this.dir.resolve(filename + "_temp.csv");      
    }

    public List<BusRecordEntity> parse() {
        if (!Files.exists(this.finalDataPath)) {
            logger.info("Cleaning data");
            cleanData();
        }
        try {
            CsvToBean<BusRecordEntity> csvToBean = new CsvToBeanBuilder<BusRecordEntity>(new CSVReader(new FileReader(this.finalDataPath.toString())))
                .withType(BusRecordEntity.class)
                .build();
            List<BusRecordEntity> busRecords = csvToBean.parse();
            return busRecords;
        } catch (FileNotFoundException e) {
            CsvParsingException exception = new CsvParsingException(e);
            logger.error("Clean data not found at: " + this.finalDataPath.toString(), exception);
            throw exception;
        }
    }

    private void cleanData() {
        // Handle values resulting in extra column
        try (
            BufferedReader br = new BufferedReader(new FileReader(rawDataPath.toString()));
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.finalDataPath.toString()));
        ) {
            // Removing values that will result in an extra column 
            String errorString1 = " ( non-public,for GEO)";
            String errorString2 = " (non-public,for GEO)";

            bw.write(br.readLine());

            bw.newLine();
                
            for (int i = 0; i < NUM_ROWS; i++) {
                String rawRow = br.readLine();
                rawRow = rawRow.replace(errorString1, "(non-public for GEO)");
                rawRow = rawRow.replace(errorString2, "(non-public for GEO)");            
                bw.write(rawRow);
                bw.newLine();
            }
        } catch (IOException e) {
            CsvParsingException exception = new CsvParsingException(e);
            logger.error("IOException during comma removal", e);
            throw exception;
        }

        // Handle Null or NA values
        try (
            CSVReader reader = new CSVReader(new FileReader(this.finalDataPath.toString()));
            CSVWriter writer = new CSVWriter(new FileWriter(this.tempDataPath.toString()));
        ) {
            writer.writeNext(reader.readNext());

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    nextLine = cleanRow(nextLine);
                    writer.writeNext(nextLine);
                } catch (NumberFormatException e) {
                    logger.error("Number format exception for row: {}", Arrays.toString(nextLine), e);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid latitude/longitude values for row: {}", Arrays.toString(nextLine), e);
                }
            }
        } catch (IOException | CsvValidationException e) {
            CsvParsingException exception = new CsvParsingException(e);
            logger.error("IOException occurred while cleaning row", exception);
            throw exception;
        }

        try {
            Files.delete(this.finalDataPath);
            Files.move(this.tempDataPath, this.finalDataPath);        
        } catch (IOException e) {
            CsvParsingException exception = new CsvParsingException(e);
            logger.error("There was an error creating the clean data file", exception);
            throw exception;
        }
    }

    private String[] cleanRow(String[] row) throws NumberFormatException, IllegalArgumentException {
        if (row[Columns.DistanceFromStop.getIndex()].equals("NA")) {
            row[Columns.DistanceFromStop.getIndex()] = "";
        }

        if (row[Columns.ExpectedArrivalTime.getIndex()].equals("NA")) {
            row[Columns.ExpectedArrivalTime.getIndex()] = "";
        }

        double latitude = Double.parseDouble(row[Columns.VehicleLocation_Latitude.getIndex()]);
        double longitude = Double.parseDouble(row[Columns.VehicleLocation_Longitude.getIndex()]);

        if (latitude > 90.0 || latitude < -90.0 || longitude > 180.0 || longitude < -180.0) {
            throw new IllegalArgumentException("Latitude or longitude values are out of range.");
        }

        return row;
    }
}
