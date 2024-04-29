package com.jerry.busappbackend.util;

public enum Columns {
    RecordedAtTime(0, "RecordedAtTime"),
    DirectionRef(1, "DirectionRef"),
    PublishedLineName(2, "PublishedLineName"),
    OriginName(3, "OriginName"),
    OriginLat(4, "OriginLat"),
    OriginLong(5, "OriginLong"),
    DestinationName(6, "DestinationName"),
    DestinationLat(7, "DestinationLat"),
    DestinationLong(8, "DestinationLong"),
    VehicleRef(9, "VehicleRef"),
    VehicleLocation_Latitude(10, "VehicleLocation.Latitude"),
    VehicleLocation_Longitude(11, "VehicleLocation.Longitude"),
    NextStopPointName(12, "NextStopPointName"),
    ArrivalProximityText(13, "ArrivalProximityText"),
    DistanceFromStop(14, "DistanceFromStop"),
    ExpectedArrivalTime(15, "ExpectedArrivalTime"),
    ScheduledArrivalTime(16, "ScheduledArrivalTime");

    private final int index;
    private final String columnName;

    Columns(int index, String columnName) {
        this.index = index;
        this.columnName = columnName;
    }

    public int getIndex() {
        return index;
    }

    public String getColumnName() {
        return columnName;
    }
}
