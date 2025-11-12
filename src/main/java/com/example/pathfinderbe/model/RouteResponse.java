package com.example.pathfinderbe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    private Object geometry;
    private double duration; // w sekundach
    private double distance; // w metrach
}