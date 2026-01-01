package com.example.pathfinderbe.dto.route;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * geometry is the Mapbox Directions route geometry (GeoJSON object).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanResponse {
    private JsonNode geometry; // GeoJSON geometry node you can pass to mapboxgl
    private double duration;   // seconds
    private double distance;   // meters
}
