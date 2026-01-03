package com.example.pathfinderbe.service;

import com.example.pathfinderbe.dto.route.GeometryDto;
import com.example.pathfinderbe.dto.route.RoutePlanRequest;
import com.example.pathfinderbe.dto.route.RoutePlanResponse;
import com.example.pathfinderbe.dto.route.WaypointDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RoutePlannerService {

    private static final double WALK_SPEED_METERS_PER_MINUTE = 83.0;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mapbox.api.url}")
    private String mapboxApiUrl;

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

    /**
     * Plans a circular walking route based on start point and duration.
     */
    public RoutePlanResponse planRoute(RoutePlanRequest request) {

        // 1. Calc distance
        double expectedDistanceMeters =
                request.getDurationMinutes() * WALK_SPEED_METERS_PER_MINUTE;

        // 2. Radius of circle
        double radiusKm = (expectedDistanceMeters / 1000.0) / (2 * Math.PI);
        double radiusDeg = radiusKm / 111.0;

        // 3. Generate points on circle border
        List<double[]> points = generateCirclePoints(
                request.getStart().getLatitude(),
                request.getStart().getLongitude(),
                radiusDeg
        );

        // 4. Build mapbox url
        String coordinates = buildCoordinateString(points);
        String url = buildMapboxUrl(coordinates);

        // 5. Call MapBox API
        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity(url, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to retrieve route from Mapbox API");
        }

        JsonNode routeNode = response.getBody()
                .path("routes")
                .get(0);

        // 6. Map response
        GeometryDto geometry = new GeometryDto(
                extractCoordinates(routeNode.path("geometry").path("coordinates"))
        );

        double distanceMeters = routeNode.path("distance").asDouble();

        // 7. Make Response DTO
        return new RoutePlanResponse(
                null, // routeId po zapisie do DB
                geometry,
                distanceMeters,
                request.getDurationMinutes(),
                request.getWaypoints()
        );
    }

    private List<double[]> generateCirclePoints(
            double startLat,
            double startLon,
            double radiusDeg
    ) {
        int pointsCount = 8;
        List<double[]> points = new ArrayList<>();

        for (int i = 0; i < pointsCount; i++) {
            double angle = 2 * Math.PI * i / pointsCount;
            double lat = startLat + radiusDeg * Math.cos(angle);
            double lon = startLon + radiusDeg * Math.sin(angle);
            points.add(new double[]{lon, lat});
        }
        points.add(new double[]{startLon, startLat});
        return points;
    }

    private String buildCoordinateString(List<double[]> points) {
        return points.stream()
                .map(p -> String.format(
                        Locale.US,
                        "%.6f,%.6f",
                        p[0],
                        p[1]
                ))
                .reduce((a, b) -> a + ";" + b)
                .orElseThrow();
    }

    private String buildMapboxUrl(String coordinates) {
        return String.format(
                "%s/%s?geometries=geojson&overview=full&access_token=%s",
                mapboxApiUrl,
                coordinates,
                mapboxApiKey
        );
    }

    private List<List<Double>> extractCoordinates(JsonNode coordinatesNode) {
        List<List<Double>> coordinates = new ArrayList<>();

        for (JsonNode node : coordinatesNode) {
            coordinates.add(List.of(
                    node.get(0).asDouble(),
                    node.get(1).asDouble()
            ));
        }
        return coordinates;
    }
}