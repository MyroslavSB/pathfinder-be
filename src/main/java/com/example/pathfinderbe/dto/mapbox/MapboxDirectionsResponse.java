package com.example.pathfinderbe.dto.mapbox;

import lombok.Data;

import java.util.List;

@Data
public class MapboxDirectionsResponse {
    private List<Route> routes;

    @Data
    public static class Route {
        private double duration;
        private double distance;
        private Object geometry;
    }
}
