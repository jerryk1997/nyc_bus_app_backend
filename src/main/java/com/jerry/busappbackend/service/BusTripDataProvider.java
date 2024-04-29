package com.jerry.busappbackend.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jerry.busappbackend.entity.BusRecordEntity;
import com.jerry.busappbackend.model.BusTrip;
import com.jerry.busappbackend.util.CsvParser;
import com.jerry.busappbackend.util.Timer;

import jakarta.annotation.PostConstruct;

/**
 * Service class for serving up the bus trip data. 
 * 
 * This class handles the logic of generating the bus trip data from the pre processed bus records.
 * Bus records will be grouped into trips based on 
 *  - PublishedLine
 *  - Vehicle Reference
 *  - Direction
 *  - Origin and destination
 *  - Time difference between records (ie. Records that differ too much in time are considered to
 *  belong to another trip)
 * 
 * BusTrip objects will be created and stored, and can be queried using either published line name 
 * or vehicle reference. 
 * 
 * @author Jerry
 */
@Service
public class BusTripDataProvider {
    private final long TRIP_WINDOW = 25;
    
    private List<BusRecordEntity> busRecords; 
    private Map<String, List<BusRecordEntity>> busRecordsMap;

    // Maps each search param (key of each map) to their trips, by referencing its index
    // in the "trips" ArrayList
    private HashMap<String, List<Integer>> pubLineNameToTripIndex = new HashMap<>();
    private HashMap<String, List<Integer>> vehRefToTripIndex = new HashMap<>();

    private List<BusTrip> trips;

    @Autowired
    CsvParser parser;

    @Autowired
    Timer timer;
    
    public BusTripDataProvider() {}

    /**
     * Initializes data from the parsed records by first doing a preliminary 
     * This method is automatically invoked after the service is instantiated and dependencies are injected.
     */
    @PostConstruct
    private void initialiseData() {
        this.busRecords = this.parser.parse();
        this.busRecordsMap = this.groupRecordsByCompositeKey();
        this.trips = this.constructTripsFromRecords();
    }

    // ================== UTILS ==================
    
    /**
     * Adds a trip to the list of trips based on provided records and trip metadata.
     * Also updates mapping indexes for quick lookup.
     *
     * @param trips List of BusTrip to which the new trip will be added.
     * @param tripRecords List of BusRecordEntity representing the records for a single trip.
     * @param publishLineName The published line name of the trip.
     * @param vehicleRef The vehicle reference of the trip.
     */
    private void addTrip(List<BusTrip> trips, List<BusRecordEntity> tripRecords, String publishLineName, String vehicleRef) {
        BusTrip newTrip = new BusTrip(tripRecords);
        trips.add(newTrip);

        this.pubLineNameToTripIndex.putIfAbsent(publishLineName, new ArrayList<Integer>());
        this.vehRefToTripIndex.putIfAbsent(vehicleRef, new ArrayList<Integer>());

        int index = trips.size() - 1;
        this.pubLineNameToTripIndex.get(publishLineName).add(index);
        this.vehRefToTripIndex.get(vehicleRef).add(index);
    }

     /**
     * Checks if a record is valid based on certain criteria:<br></br>
     * 
     * - ExpectedArrival time is not null<br></br>
     * - ArrivalProximityText is not NA<br></br>
     * - DistanceFromStop is not null<br></br>
     *
     * @param record The BusRecordEntity to check.
     * @return true if the record is valid, false otherwise.
     */
    private boolean isValidRecord(BusRecordEntity record) {
        return record.getExpectedArrivalTime() != null &&
            !record.getArrivalProximityText().equals("NA") &&
            record.getDistanceFromStop() != null;
    } 

    /**
     * Creates a composite key for a bus record, used to preliminarily 
     * group records that might belong to the same trip.<br></br>
     * <br></br>
     * Records belonging to the same trip will have the same composite key, 
     * but not vice versa.<br></br>
     * <br></br>
     * Composite key elements:<br></br>
     * - Published line name<br></br>
     * - Vehicle reference<br></br>
     * - Direction of travel<br></br>
     * - Origin and destination<br></br>
     *
     * @param record The BusRecordEntity for which to create the key.
     * @return A string representing the composite key.
     */
    private String createCompositeKey(BusRecordEntity record) {
        return Stream.of(
            record.getPublishedLineName(),
            record.getVehicleRef(),
            String.valueOf(record.getDirectionRef()),
            record.getOriginName(),
            record.getDestinationName()
        ).collect(Collectors.joining("|"));
    }

    /**
     * Validate groups records by their composite keys.
     *
     * @return A map of grouped bus records, where each key is a composite key and each value is a list of records.
     */
    private Map<String, List<BusRecordEntity>> groupRecordsByCompositeKey() {
        return this.busRecords.stream()
            .filter(this::isValidRecord)
            .sorted()
            .collect(Collectors.groupingBy(this::createCompositeKey));
    }

    /**
     * Generates all bus trips from the grouped bus records. busRecordMaps must be populated before this
     * method is called. 
     *
     * @return A list of BusTrip objects created from the grouped records.
     */
    private List<BusTrip> constructTripsFromRecords() {
        List<BusTrip> result = new ArrayList<>();
        final int publishLineKeyIndex = 0;
        final int vehicleRefKeyIndex = 1;
    
        for (String tripCompositeKey : this.busRecordsMap.keySet()) {
            List<BusRecordEntity> records = this.busRecordsMap.get(tripCompositeKey);
            String publishLineName = tripCompositeKey.split("\\|")[publishLineKeyIndex];
            String vehicleRef = tripCompositeKey.split("\\|")[vehicleRefKeyIndex];
    
            List<BusRecordEntity> currentTripRecords = new ArrayList<>();
            BusRecordEntity prevRecord = null;
    
            for (BusRecordEntity record : records) {
                if (currentTripRecords.isEmpty() || isNewTrip(prevRecord, record)) {
                    if (!currentTripRecords.isEmpty()) {
                        addTrip(result, currentTripRecords, publishLineName, vehicleRef);
                        currentTripRecords = new ArrayList<>();
                    }
                    currentTripRecords.add(record);
                } else {
                    currentTripRecords.add(record);
                }
                prevRecord = record;
            }
    
            if (!currentTripRecords.isEmpty()) {
                addTrip(result, currentTripRecords, publishLineName, vehicleRef);
            }
        }
        return result;
    }

    /**
     * Determines whether the current record belongs to the current trip or a new trip.
     *
     * @param prevRecord The previous bus record.
     * @param currentRecord The current bus record to evaluate.
     * @return true if a new trip should be started, false otherwise.
     */
    private boolean isNewTrip(BusRecordEntity prevRecord, BusRecordEntity currentRecord) {
        return ChronoUnit.MINUTES.between(prevRecord.getExpectedArrivalTime(), currentRecord.getExpectedArrivalTime()) > TRIP_WINDOW;
    }

    // ================== GETTERS ==================
    public List<String> getAllPublishedLineName() {
        List<String> result = new ArrayList<>(pubLineNameToTripIndex.keySet());
        Collections.sort(result);
        return result;
    }

    public List<String> getAllVehicleRef() {
        List<String> result = new ArrayList<>(vehRefToTripIndex.keySet());
        Collections.sort(result);
        return result;
    }

    public List<BusTrip> getTrips() {
        return trips;
    }

    public List<BusTrip> getTripByPublishedLineName(String publishedLineName) {
        return getTripByType("publishedLineName", publishedLineName);
    }

    public List<BusTrip> getTripByVehicleRef(String vehicleRef) {
        return getTripByType("vehicleRef", vehicleRef);
    }

    private List<BusTrip> getTripByType (String type, String value) {
        HashMap<String, List<Integer>> map = type.equals("publishedLineName") ? pubLineNameToTripIndex : vehRefToTripIndex;
        List<BusTrip> result = new ArrayList<>();
        List<Integer> tripIndexes = map.get(value);
        for (Integer index : tripIndexes) {
            result.add(trips.get(index));
        }

        return result;
    }
}
