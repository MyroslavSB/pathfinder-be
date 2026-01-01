package com.example.pathfinderbe.service;

import com.example.pathfinderbe.dto.route.RoutePlanResponse;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mapbox.api.url}")
    private String mapboxApiUrl;

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

    public RoutePlanResponse planCircularRoute(double startLat, double startLon, int durationMinutes) {
        try {
            // estimate walk speed ~5km/h (≈83m/min)
            double expectedDistanceKm = (durationMinutes * 83.0) / 1000.0;
            double radius = expectedDistanceKm / (2 * Math.PI); // circle radius (km)

            // convert km → degrees (approx)
            double radiusDeg = radius / 111.0;

            // Generate points around the start
            List<double[]> coords = new ArrayList<>();
            int points = 8; // nice smooth circle
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double lat = startLat + radiusDeg * Math.cos(angle);
                double lon = startLon + radiusDeg * Math.sin(angle);
                coords.add(new double[]{lon, lat});
            }
            coords.add(new double[]{startLon, startLat}); // close the loop

            // build unencoded coordinate string
            String coordinates = coords.stream()
                    .map(c -> String.format(Locale.US, "%.6f,%.6f", c[0], c[1]))
                    .reduce((a, b) -> a + ";" + b)
                    .orElseThrow();

            // build full URL
            String url = String.format(
                    "%s/%s?geometries=geojson&overview=full&access_token=%s",
                    mapboxApiUrl,
                    coordinates,
                    mapboxApiKey
            );

            // call Mapbox
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode route = response.getBody().path("routes").get(0);
                return new RoutePlanResponse(
                        route.path("geometry"),
                        route.path("distance").asDouble(),
                        route.path("duration").asDouble()
                );
            }

            return new RoutePlanResponse(null, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
            return new RoutePlanResponse(null, 0, 0);
        }
    }
}