package com.example.pathfinderbe.route;

import com.example.pathfinderbe.dto.route.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RoutePlannerService {

    private static final double WALK_SPEED_METERS_PER_MINUTE = 83.0;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mapbox.api.url}")
    private String mapboxApiUrl;

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

    public RoutePlanResponse planRoute(RoutePlanRequest request) {

        validate(request);

        if (request.getRouteType() == ERouteType.CIRCULAR) {
            return planCircularRoute(request);
        }

        return planPointToPointRoute(request);
    }

    private void validate(RoutePlanRequest r) {
        if (r.getStart() == null || r.getEnd() == null) {
            throw new IllegalArgumentException("Start and End are required");
        }
    }

    private RoutePlanResponse planCircularRoute(RoutePlanRequest request) {

        double targetMeters = expectedDistance(request.getDuration());
        double radiusKm = (targetMeters / 1000.0) / (2 * Math.PI);
        double radiusDeg = radiusKm / 111.0;

        List<double[]> points = new ArrayList<>();
        points.add(coord(request.getStart()));

        for (CoordinateDto wp : request.getWaypoints()) {
            points.add(coord(wp));
        }

        points.addAll(generateCirclePoints(
                request.getStart().getLat(),
                request.getStart().getLng(),
                radiusDeg
        ));

        return callMapbox(points, request);
    }

    private RoutePlanResponse planPointToPointRoute(RoutePlanRequest request) {

        List<double[]> base = new ArrayList<>();
        base.add(coord(request.getStart()));

        for (CoordinateDto wp : request.getWaypoints()) {
            base.add(coord(wp));
        }

        base.add(coord(request.getEnd()));

        RoutePlanResponse route = callMapbox(base, request);

        double target = expectedDistance(request.getDuration());
        double actual = route.getDistanceMeters();

        if (actual < target * 0.95) {
            double missing = target - actual;
            List<double[]> extended = extendRouteWithLoop(base, missing);
            return callMapbox(extended, request);
        }

        return route;
    }

    private List<double[]> extendRouteWithLoop(List<double[]> base, double missingMeters) {

        double radiusMeters = missingMeters / (2 * Math.PI);
        double radiusDeg = (radiusMeters / 1000.0) / 111.0;

        double[] end = base.get(base.size() - 1);

        List<double[]> extended = new ArrayList<>(base);
        extended.addAll(generateCirclePoints(end[1], end[0], radiusDeg));
        extended.add(end);

        return extended;
    }

    private RoutePlanResponse callMapbox(List<double[]> points, RoutePlanRequest request) {

        String coords = buildCoordinateString(points);
        String url = buildMapboxUrl(coords);

        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity(url, JsonNode.class);

        JsonNode route = response.getBody().path("routes").get(0);

        GeometryDto geometry = new GeometryDto(
                extractCoordinates(route.path("geometry").path("coordinates"))
        );

        return new RoutePlanResponse(
                geometry,
                route.path("distance").asDouble(),
                request.getDuration(),
                request.getWaypoints()
        );
    }

    private double expectedDistance(int minutes) {
        return minutes * WALK_SPEED_METERS_PER_MINUTE;
    }

    private double[] coord(CoordinateDto c) {
        return new double[]{c.getLng(), c.getLat()};
    }

    private List<double[]> generateCirclePoints(double lat, double lng, double radiusDeg) {
        int count = 8;
        List<double[]> pts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            pts.add(new double[]{
                    lng + radiusDeg * Math.sin(angle),
                    lat + radiusDeg * Math.cos(angle)
            });
        }
        return pts;
    }

    private String buildCoordinateString(List<double[]> points) {
        return points.stream()
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p[0], p[1]))
                .reduce((a, b) -> a + ";" + b)
                .orElseThrow();
    }

    private String buildMapboxUrl(String coordinates) {
        return String.format(
                "%s/%s?geometries=geojson&overview=full&access_token=%s&steps=true",
                mapboxApiUrl,
                coordinates,
                mapboxApiKey
        );
    }

    private List<List<Double>> extractCoordinates(JsonNode node) {
        List<List<Double>> list = new ArrayList<>();
        for (JsonNode n : node) {
            list.add(List.of(n.get(0).asDouble(), n.get(1).asDouble()));
        }
        return list;
    }
}
