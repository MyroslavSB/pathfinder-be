package com.example.pathfinderbe.dto.route;

import java.util.List;

public class RoutePlanResponse {

    private Long routeId;
    private GeometryDto geometry;
    private Double distanceMeters;
    private Integer durationMinutes;
    private List<WaypointDto> waypoints;

    public RoutePlanResponse(
            Long routeId,
            GeometryDto geometry,
            Double distanceMeters,
            Integer durationMinutes,
            List<WaypointDto> waypoints
    ) {
        this.routeId = routeId;
        this.geometry = geometry;
        this.distanceMeters = distanceMeters;
        this.durationMinutes = durationMinutes;
        this.waypoints = waypoints;
    }

    public Long getRouteId() {
        return routeId;
    }

    public GeometryDto getGeometry() {
        return geometry;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public List<WaypointDto> getWaypoints() {
        return waypoints;
    }
}
