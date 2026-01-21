package com.example.pathfinderbe.dto.route;

import java.util.List;

public class GeometryDto {

    /**
     * GeoJSON LineString
     * format: [[lng, lat], [lng, lat], ...]
     */
    private String type = "LineString";

    private List<List<Double>> coordinates;

    public GeometryDto(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}
