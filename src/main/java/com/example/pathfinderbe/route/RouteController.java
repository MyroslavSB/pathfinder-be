package com.example.pathfinderbe.route;

import com.example.pathfinderbe.dto.route.RoutePlanRequest;
import com.example.pathfinderbe.dto.route.RoutePlanResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RoutePlannerService routePlannerService;

    public RouteController(RoutePlannerService routePlannerService) {
        this.routePlannerService = routePlannerService;
    }

    @PostMapping("/plan")
    public ResponseEntity<RoutePlanResponse> planRoute(
            @Valid
            @RequestBody
            RoutePlanRequest request,
            Authentication authentication
    ) {
        // authentication.getName() -> email / username z JWT
        RoutePlanResponse response =
                routePlannerService.planRoute(request);

        return ResponseEntity.ok(response);
    }
}
