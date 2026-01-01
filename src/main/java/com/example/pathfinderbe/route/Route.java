package com.example.pathfinderbe.route;

import com.example.pathfinderbe.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Double distanceMeters;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Waypoint> waypoints;

    protected Route() {
    }

    public Route(User user, Integer durationMinutes, Double distanceMeters) {
        this.user = user;
        this.durationMinutes = durationMinutes;
        this.distanceMeters = distanceMeters;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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
