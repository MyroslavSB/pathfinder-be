package com.example.pathfinderbe.dto.route;


import java.util.List;

public class RoutePlanRequest {

    private CoordinateDto start;
    private CoordinateDto end;
    private List<CoordinateDto> waypoints;
    private Integer duration;
    private ERouteType routeType;

    public CoordinateDto getStart() {
        return start;
    }

    public CoordinateDto getEnd() {
        return end;
    }

    public List<CoordinateDto> getWaypoints() {
        return waypoints;
    }

    public Integer getDuration() {
        return duration;
    }

    public ERouteType getRouteType() {
        return routeType;
    }
}
