package com.example.pathfinderbe.dto.route;

import java.time.LocalDateTime;

public class RouteSummaryDto {

    private Long id;
    private Double distanceMeters;
    private Integer durationMinutes;
    private LocalDateTime createdAt;

    public RouteSummaryDto(
            Long id,
            Double distanceMeters,
            Integer durationMinutes,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.distanceMeters = distanceMeters;
        this.durationMinutes = durationMinutes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
