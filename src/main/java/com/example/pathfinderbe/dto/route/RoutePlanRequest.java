package com.example.pathfinderbe.dto.route;

import lombok.Data;

@Data
public class RoutePlanRequest {
    private double startLat;
    private double startLon;
    private int durationMinutes; // requested walk duration in minutes
    private Integer pointsCount; // optional, number of circle waypoints (defaults to 8)
}
