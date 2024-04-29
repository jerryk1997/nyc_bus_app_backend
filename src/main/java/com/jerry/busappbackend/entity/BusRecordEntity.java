package com.jerry.busappbackend.entity;

import java.time.LocalDateTime;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

public class BusRecordEntity implements Comparable<BusRecordEntity> {
    @CsvBindByName(column = "DirectionRef")
    private int directionRef;

    @CsvBindByName(column = "PublishedLineName")
    private String publishedLineName;

    @CsvBindByName(column = "VehicleRef")
    private String vehicleRef;

    @CsvBindByName(column = "OriginName")
    private String originName;

    @CsvBindByName(column = "DestinationName")
    private String destinationName;

    @CsvBindByName(column = "VehicleLocation.Latitude")
    private double vehicleLocationLatitude;

    @CsvBindByName(column = "VehicleLocation.Longitude")
    private double vehicleLocationLongitude;

    @CsvBindByName(column = "ArrivalProximityText")
    private String arrivalProximityText;

    @CsvBindByName(column = "DistanceFromStop")
    private Integer distanceFromStop;

    @CsvBindByName(column = "ExpectedArrivalTime")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedArrivalTime;

    // ============================ Getters and Setters ============================

    public int getDirectionRef() {
        return this.directionRef;
    }

    public void setDirectionRef(int directionRef) {
        this.directionRef = directionRef;
    }

    public String getPublishedLineName() {
        return this.publishedLineName;
    }

    public void setPublishedLineName(String publishedLineName) {
        this.publishedLineName = publishedLineName;
    }

    public String getVehicleRef() {
        return this.vehicleRef;
    }

    public void setVehicleRef(String vehicleRef) {
        this.vehicleRef = vehicleRef;
    }

    public String getOriginName() {
        return this.originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return this.destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public double getVehicleLocationLatitude() {
        return this.vehicleLocationLatitude;
    }

    public void setVehicleLocationLatitude(double vehicleLocationLatitude) {
        this.vehicleLocationLatitude = vehicleLocationLatitude;
    }

    public double getVehicleLocationLongitude() {
        return this.vehicleLocationLongitude;
    }

    public void setVehicleLocationLongitude(double vehicleLocationLongitude) {
        this.vehicleLocationLongitude = vehicleLocationLongitude;
    }

    public String getArrivalProximityText() {
        return this.arrivalProximityText;
    }

    public void setArrivalProximityText(String arrivalProximityText) {
        this.arrivalProximityText = arrivalProximityText;
    }

    public Integer getDistanceFromStop() {
        return this.distanceFromStop;
    }

    public void setDistanceFromStop(int distanceFromStop) {
        this.distanceFromStop = distanceFromStop;
    }

    public LocalDateTime getExpectedArrivalTime() {
        return this.expectedArrivalTime;
    }

    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    // ========================================================


    // ============================ Overrides ============================
    @Override
    public int compareTo(BusRecordEntity o) {
        if (this.expectedArrivalTime == null && o.getExpectedArrivalTime() == null) {
            return 0;
        } else if (this.expectedArrivalTime == null) {
            return -1;
        } else if (o.getExpectedArrivalTime() == null) {
            return 1;
        }
        return this.expectedArrivalTime.compareTo(o.getExpectedArrivalTime());
    }

    @Override
    public String toString() {
        return "{" +
            " directionRef='" + getDirectionRef() + "'" +
            ", publishedLineName='" + getPublishedLineName() + "'" +
            ", vehicleRef='" + getVehicleRef() + "'" +
            ", originName='" + getOriginName() + "'" +
            ", destinationName='" + getDestinationName() + "'" +
            ", vehicleLocationLatitude='" + getVehicleLocationLatitude() + "'" +
            ", vehicleLocationLongitude='" + getVehicleLocationLongitude() + "'" +
            ", arrivalProximityText='" + getArrivalProximityText() + "'" +
            ", distanceFromStop='" + getDistanceFromStop() + "'" +
            ", expectedArrivalTime='" + getExpectedArrivalTime() + "'" +
            "}";
    }
}
