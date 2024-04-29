package com.jerry.busappbackend.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.jerry.busappbackend.entity.BusRecordEntity;
import com.jerry.busappbackend.util.Columns;

/**
 * Represents a bus trip constructed from a series of bus record entities. This model encapsulates
 * details about a specific bus route segment, including the vehicle, route, and timestamps
 * associated with its journey, as well as detailed geographic coordinates for the trip.
 * <p>
 * This class provides methods to access properties such as the line name, vehicle reference,
 * direction, origin and destination names, start and end times, and detailed coordinates
 * of each point along the trip's path.
 */
public class BusTrip {
    private final String publishedLineName;
    private final String vehicleRef;
    private final int directionRef;
 
    private final String originName;
    private final String destinationName;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final double[][] coords;
    private final String[][] allPointInfo;
    
    /**
     * Constructs a new BusTrip object based on a list of BusRecordEntity objects.
     * Each record is assumed to be part of the same trip. The constructor aggregates
     * information from these records to establish trip details.
     *
     * @param tripRecords a list of {@link BusRecordEntity} objects representing records for the trip
     * @throws IllegalArgumentException if tripRecords is empty
     */
    public BusTrip(List<BusRecordEntity> tripRecords) {
        BusRecordEntity firstRecord = tripRecords.get(0);
        
        this.publishedLineName = firstRecord.getPublishedLineName();
        this.vehicleRef = firstRecord.getVehicleRef();
        this.directionRef = firstRecord.getDirectionRef();
        this.originName = firstRecord.getOriginName();
        this.destinationName = firstRecord.getDestinationName();
        this.startTime = tripRecords.get(0).getExpectedArrivalTime();
        this.endTime = tripRecords.get(tripRecords.size() - 1).getExpectedArrivalTime();

        int numRecords = tripRecords.size();

        coords = new double[numRecords][2];
        allPointInfo = new String[8 + (numRecords * 4)][2];

        allPointInfo[0][0] = "VehicleRef";
        allPointInfo[0][1] = this.vehicleRef;

        allPointInfo[1][0] = "PublishedLineName";
        allPointInfo[1][1] = this.publishedLineName;

        allPointInfo[2][0] = "DirectionRef";
        allPointInfo[2][1] = this.directionRef + "";

        allPointInfo[3][0] = "OriginName";
        allPointInfo[3][1] = this.originName;

        allPointInfo[4][0] = "DestinationName";
        allPointInfo[4][1] = this.destinationName;

        allPointInfo[5][0] = "StartTime";
        allPointInfo[5][1] = this.startTime.toString();

        allPointInfo[6][0] = "EndTime";
        allPointInfo[6][1] = this.endTime.toString();

        allPointInfo[7][0] = "NumOfPoints";
        allPointInfo[7][1] = this.coords.length + "";

        for (int i = 0; i < numRecords; i++) {
            BusRecordEntity record = tripRecords.get(i);

            coords[i][0] = record.getVehicleLocationLongitude(); // long
            coords[i][1] = record.getVehicleLocationLatitude(); // lat
            
            this.allPointInfo[i * 4 + 8][0] = "Point " + (i + 1) + " geom";
            this.allPointInfo[i * 4 + 8][1] = "MyLatLong(longitude=" + record.getVehicleLocationLongitude() + ", latitude=" + record.getVehicleLocationLatitude() + ")";

            this.allPointInfo[(i * 4) + 9][0] = "Point " + (i + 1) + " arrival";
            this.allPointInfo[(i * 4) + 9][1] = record.getArrivalProximityText();

            this.allPointInfo[(i * 4) + 10][0] = "Point " + (i + 1) + " dist from stop";
            this.allPointInfo[(i * 4) + 10][1] = record.getDistanceFromStop() + ""; 

            this.allPointInfo[(i * 4) + 11][0] = "Point " + (i + 1) + " time";
            this.allPointInfo[(i * 4) + 11][1] = record.getExpectedArrivalTime().toString();
        }
    }

    public String getPublishedLineName() {
        return this.publishedLineName;
    }

    public String getVehicleRef() {
        return this.vehicleRef;
    }


    public int getDirectionRef() {
        return this.directionRef;
    }

    public String getOriginName() {
        return this.originName;
    }


    public String getDestinationName() {
        return this.destinationName;
    }


    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public double[][] getCoords() {
        return this.coords;
    }

    public String[][] getAllPointInfo() {
        return this.allPointInfo;
    }


    private String coordToString() {
        StringBuilder result = new StringBuilder();
        result.append("[\n"); 
        for (double[] coord : coords) {
            result.append("\t" + Arrays.toString(coord) + "\n");
        }
        result.append("]\n");
        return result.toString();
    }

    private String pointInfoToString() {
        StringBuilder result = new StringBuilder();
        for (String[] pointInfo : allPointInfo) {
            result.append(Arrays.toString(pointInfo) + "\n");
        }

        
        return result.substring(0, result.length() - 1);
    }
    

    @Override
    public String toString() {
        return (
            "\n\n\n ================================================\n" + 
            Columns.VehicleRef.getColumnName() + ": " + this.vehicleRef + "\n" +
            Columns.PublishedLineName.getColumnName() + ": " + this.publishedLineName + "\n" +
            Columns.DirectionRef.getColumnName() + ": " + this.directionRef + "\n" +
            Columns.OriginName.getColumnName() + ": " + this.originName + "\n" +
            Columns.DestinationName.getColumnName() + ": " + this.destinationName + "\n" +
            "Start time: " + this.startTime + "\n" +
            "End time: " + this.endTime + "\n" +
            "Coords: \n" + coordToString() +
            pointInfoToString()            
        );
    }
}
