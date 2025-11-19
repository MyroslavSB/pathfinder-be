package com.example.pathfinderbe.controller;

import com.example.pathfinderbe.model.RoutePlanRequest;
import com.example.pathfinderbe.model.RoutePlanResponse;
import com.example.pathfinderbe.service.RoutePlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RoutePlannerService routePlannerService;

    @PostMapping("/plan")
    public ResponseEntity<RoutePlanResponse> planRoute(@RequestBody RoutePlanRequest request) {
        try {
            RoutePlanResponse resp = routePlannerService.planCircularRoute(
                    request.getStartLat(),
                    request.getStartLon(),
                    request.getDurationMinutes()
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            // for debugging/testing we return 500 with message
            return ResponseEntity.status(500).body(new RoutePlanResponse(null, 0, 0));
        }
    }
}
