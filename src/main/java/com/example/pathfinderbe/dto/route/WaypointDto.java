package com.example.pathfinderbe.dto.route;

public class WaypointDto {

    private String name;
    private Double latitude;
    private Double longitude;
    private Integer order;

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getOrder() {
        return order;
    }
}
