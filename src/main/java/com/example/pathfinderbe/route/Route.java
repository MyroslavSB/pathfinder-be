package com.example.pathfinderbe.route;

import com.example.pathfinderbe.user.User;
import java.time.LocalDateTime;
import java.util.List;

public class Route {

    private User user;
    private Integer durationMinutes;
    private Double distanceMeters;
    private LocalDateTime createdAt;
    private List<Waypoint> waypoints;

    public Route(User user, Integer durationMinutes, Double distanceMeters, List<Waypoint> waypoints) {
        this.user = user;
        this.durationMinutes = durationMinutes;
        this.distanceMeters = distanceMeters;
        this.createdAt = LocalDateTime.now();
        this.waypoints = waypoints;
    }

    public User getUser() {
        return user;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }
}
