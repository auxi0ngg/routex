package com.routex.tracking.service;

import com.routex.tracking.dto.DriverLocationUpdate;
import com.routex.tracking.entity.TrackingEvent;
import java.time.Instant;
import java.util.*;

public interface TrackingService {
    void updateDriverLocation(DriverLocationUpdate update);
    Optional<Map<String, Object>> getDriverCurrentLocation(String driverId);
    Optional<Map<String, Object>> getShipmentCurrentLocation(String shipmentId);
    List<TrackingEvent> getShipmentTrackingHistory(UUID shipmentId);
    List<TrackingEvent> getDriverRoutePlayback(UUID driverId, Instant from, Instant to);
    void checkGeofences(DriverLocationUpdate update);
    Map<String, Object> getAllActiveDriverLocations();
}
