package com.example.pathfinderbe.service;

import com.example.pathfinderbe.model.RoutePlanRequest;
import com.example.pathfinderbe.model.RoutePlanResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoutePlannerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mapbox.api.url}")
    private String mapboxApiUrl; // e.g. https://api.mapbox.com/directions/v5/mapbox/walking

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

    // walking speed meters per second (5 km/h)
    private static final double WALKING_SPEED_MPS = 5000.0 / 3600.0; // ≈ 1.3888889

    public RoutePlanResponse planCircularRoute(RoutePlanRequest req) throws Exception {
        double startLat = req.getStartLat();
        double startLon = req.getStartLon();
        int durationMin = req.getDurationMinutes();
        int pointsCount = (req.getPointsCount() == null || req.getPointsCount() < 3) ? 8 : req.getPointsCount();

        // Desired total length in meters (loop)
        double totalSeconds = durationMin * 60.0;
        double desiredLengthMeters = WALKING_SPEED_MPS * totalSeconds;

        // Radius of circle (meters)
        double radius = desiredLengthMeters / (2.0 * Math.PI);

        // Build list of waypoints: start -> circle points -> start
        List<double[]> coords = new ArrayList<>();
        coords.add(new double[]{startLon, startLat}); // Mapbox uses lon,lat

        for (int i = 0; i < pointsCount; i++) {
            double bearing = i * (360.0 / pointsCount);
            double[] dest = destinationPoint(startLat, startLon, radius, bearing);
            coords.add(new double[]{dest[1], dest[0]}); // store lon,lat consistent
        }

        coords.add(new double[]{startLon, startLat});

        // Build coordinates string for Mapbox: lon,lat;lon,lat;...
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coords.size(); i++) {
            double[] c = coords.get(i);
            if (i > 0) sb.append(";");
            sb.append(String.format("%f,%f", c[0], c[1]));
        }

        String url = String.format("%s/%s?geometries=geojson&overview=full&access_token=%s",
                mapboxApiUrl,
                java.net.URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8),
                mapboxApiKey);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Mapbox Directions API error: " + response.getStatusCode());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode routes = root.path("routes");
        if (!routes.isArray() || routes.size() == 0) {
            throw new RuntimeException("No routes returned by Mapbox.");
        }

        JsonNode best = routes.get(0);
        double duration = best.path("duration").asDouble(); // seconds
        double distance = best.path("distance").asDouble(); // meters
        JsonNode geometry = best.path("geometry"); // geojson geometry

        return new RoutePlanResponse(geometry, duration, distance);
    }

    /**
     * Compute destination point given start lat/lon, distance (meters) and bearing (degrees).
     * Returns double[]{lat, lon}.
     *
     * Formula based on spherical Earth.
     */
    private double[] destinationPoint(double latDeg, double lonDeg, double distanceMeters, double bearingDeg) {
        double R = 6378137.0; // Earth radius in meters (WGS84)
        double bearing = Math.toRadians(bearingDeg);
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);

        double angularDistance = distanceMeters / R;

        double destLat = Math.asin(
                Math.sin(lat) * Math.cos(angularDistance) +
                        Math.cos(lat) * Math.sin(angularDistance) * Math.cos(bearing)
        );

        double destLon = lon + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat),
                Math.cos(angularDistance) - Math.sin(lat) * Math.sin(destLat)
        );

        return new double[]{Math.toDegrees(destLat), Math.toDegrees(destLon)};
    }
}
