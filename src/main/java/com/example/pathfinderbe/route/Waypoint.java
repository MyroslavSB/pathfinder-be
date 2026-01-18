package com.example.pathfinderbe.route;

public class Waypoint {

    private String name;
    private Double latitude;
    private Double longitude;
    private Integer sequenceOrder;

    public Waypoint(String name, Double latitude, Double longitude, Integer sequenceOrder) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequenceOrder = sequenceOrder;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }
}
