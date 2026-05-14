package com.routex.tracking.dto;

public record DriverLocationUpdate(
    String driverId,
    double latitude,
    double longitude,
    double heading,
    double speedKmh,
    double accuracy,
    String activeShipmentId
) {}
