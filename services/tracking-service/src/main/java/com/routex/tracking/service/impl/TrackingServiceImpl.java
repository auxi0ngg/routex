package com.routex.tracking.service.impl;

import com.routex.tracking.dto.*;
import com.routex.tracking.entity.TrackingEvent;
import com.routex.tracking.kafka.producer.TrackingEventProducer;
import com.routex.tracking.repository.TrackingEventRepository;
import com.routex.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceImpl implements TrackingService {

    private final TrackingEventRepository trackingEventRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TrackingEventProducer eventProducer;

    private static final String DRIVER_LOCATION_KEY = "driver:location:";
    private static final String SHIPMENT_LOCATION_KEY = "shipment:location:";
    private static final Duration LOCATION_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public void updateDriverLocation(DriverLocationUpdate update) {
        // 1. Store in Redis (live state — millisecond access)
        Map<String, Object> locationData = Map.of(
            "driverId", update.driverId(),
            "latitude", update.latitude(),
            "longitude", update.longitude(),
            "heading", update.heading(),
            "speedKmh", update.speedKmh(),
            "timestamp", Instant.now().toString()
        );
        redisTemplate.opsForValue().set(
            DRIVER_LOCATION_KEY + update.driverId(),
            locationData,
            LOCATION_TTL
        );

        // 2. Persist to PostgreSQL for history
        TrackingEvent event = TrackingEvent.builder()
            .driverId(UUID.fromString(update.driverId()))
            .shipmentId(update.activeShipmentId() != null ? UUID.fromString(update.activeShipmentId()) : null)
            .latitude(BigDecimal.valueOf(update.latitude()))
            .longitude(BigDecimal.valueOf(update.longitude()))
            .heading(update.heading())
            .speedKmh(BigDecimal.valueOf(update.speedKmh()))
            .accuracy(update.accuracy())
            .eventType("LOCATION_UPDATE")
            .build();
        trackingEventRepository.save(event);

        // 3. Broadcast via WebSocket to subscribed dashboards
        messagingTemplate.convertAndSend(
            "/topic/driver/" + update.driverId(),
            locationData
        );

        // If driver has active shipment, also broadcast to shipment subscribers
        if (update.activeShipmentId() != null) {
            redisTemplate.opsForValue().set(
                SHIPMENT_LOCATION_KEY + update.activeShipmentId(),
                locationData,
                LOCATION_TTL
            );
            messagingTemplate.convertAndSend(
                "/topic/shipment/" + update.activeShipmentId(),
                locationData
            );
        }

        // 4. Publish to Kafka for analytics and notifications
        eventProducer.publishLocationUpdate(update);
    }

    @Override
    public Optional<Map<String, Object>> getDriverCurrentLocation(String driverId) {
        Object data = redisTemplate.opsForValue().get(DRIVER_LOCATION_KEY + driverId);
        if (data instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> locationMap = (Map<String, Object>) map;
            return Optional.of(locationMap);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Object>> getShipmentCurrentLocation(String shipmentId) {
        Object data = redisTemplate.opsForValue().get(SHIPMENT_LOCATION_KEY + shipmentId);
        if (data instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> locationMap = (Map<String, Object>) map;
            return Optional.of(locationMap);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEvent> getShipmentTrackingHistory(UUID shipmentId) {
        return trackingEventRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEvent> getDriverRoutePlayback(UUID driverId, Instant from, Instant to) {
        return trackingEventRepository.findByDriverIdAndCreatedAtBetween(driverId, from, to);
    }

    @Override
    public void checkGeofences(DriverLocationUpdate update) {
        // Geofence logic: check if driver is within warehouse/delivery zones
        // Publish geofence event if boundary crossed
        log.debug("Checking geofences for driver: {}", update.driverId());
        // Implementation: compare against stored geofence polygons in Redis/DB
    }

    @Override
    public Map<String, Object> getAllActiveDriverLocations() {
        Set<String> keys = redisTemplate.keys(DRIVER_LOCATION_KEY + "*");
        Map<String, Object> result = new HashMap<>();
        if (keys != null) {
            keys.forEach(key -> {
                Object val = redisTemplate.opsForValue().get(key);
                if (val != null) {
                    String driverId = key.replace(DRIVER_LOCATION_KEY, "");
                    result.put(driverId, val);
                }
            });
        }
        return result;
    }
}
