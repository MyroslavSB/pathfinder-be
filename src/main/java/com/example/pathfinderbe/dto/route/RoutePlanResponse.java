package com.example.pathfinderbe.dto.route;

import java.util.List;

public class RoutePlanResponse {

    private GeometryDto geometry;
    private Double distanceMeters;
    private Integer durationMinutes;
    private List<CoordinateDto> waypoints;

    public RoutePlanResponse(
            GeometryDto geometry,
            Double distanceMeters,
            Integer durationMinutes,
            List<CoordinateDto> waypoints
    ) {
        this.geometry = geometry;
        this.distanceMeters = distanceMeters;
        this.durationMinutes = durationMinutes;
        this.waypoints = waypoints;
    }

    public GeometryDto getGeometry() { return geometry; }
    public Double getDistanceMeters() { return distanceMeters; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public List<CoordinateDto> getWaypoints() { return waypoints; }
}
