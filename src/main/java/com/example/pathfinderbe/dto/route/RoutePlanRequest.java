package com.example.pathfinderbe.dto.route;


import java.util.List;

public class RoutePlanRequest {

    private CoordinateDto start;

    private CoordinateDto end;

    private List<WaypointDto> waypoints;

    private Integer durationMinutes;

    public CoordinateDto getStart() {
        return start;
    }

    public CoordinateDto getEnd() {
        return end;
    }

    public List<WaypointDto> getWaypoints() {
        return waypoints;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }
}
