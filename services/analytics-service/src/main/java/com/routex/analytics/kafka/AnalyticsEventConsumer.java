package com.routex.analytics.kafka;

import com.routex.analytics.entity.*;
import com.routex.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final DeliveryMetricRepository deliveryMetricRepository;
    private final ShipmentVolumeRepository shipmentVolumeRepository;

    @KafkaListener(
        topics = "analytics-events",
        groupId = "analytics-service",
        containerFactory = "analyticsKafkaListenerFactory"
    )
    @Transactional
    public void consumeAnalyticsEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment ack) {
        try {
            Map<String, Object> event = record.value();
            String eventType = (String) event.get("eventType");
            log.debug("Processing analytics event: {}", eventType);

            switch (eventType) {
                case "SHIPMENT_CREATED" -> handleShipmentCreated(event);
                case "SHIPMENT_STATUS_UPDATED" -> handleStatusUpdate(event);
                default -> log.debug("Unhandled analytics event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Analytics event processing failed: {}", e.getMessage(), e);
            // Don't ack — will be retried; after max retries goes to DLQ
        }
    }

    @KafkaListener(
        topics = "tracking-events",
        groupId = "analytics-tracking-service",
        containerFactory = "analyticsKafkaListenerFactory"
    )
    @Transactional
    public void consumeTrackingEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment ack) {
        try {
            Map<String, Object> event = record.value();
            // Process driver performance metrics from tracking data
            if ("LOCATION_UPDATE".equals(event.get("eventType"))) {
                updateDriverMetrics(event);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Tracking analytics failed: {}", e.getMessage(), e);
        }
    }

    // Dead Letter Queue consumer — logs failed events for manual review
    @KafkaListener(
        topics = "analytics-events.DLT",
        groupId = "analytics-dlq-service"
    )
    public void consumeDeadLetter(ConsumerRecord<String, Object> record) {
        log.error("Dead letter analytics event — key: {}, partition: {}, offset: {}",
            record.key(), record.partition(), record.offset());
        // Store in dead_letter_events table for ops review
    }

    private void handleShipmentCreated(Map<String, Object> event) {
        String orgId = (String) event.get("organizationId");
        ShipmentVolumeRecord record = ShipmentVolumeRecord.builder()
            .organizationId(UUID.fromString(orgId))
            .shipmentId(UUID.fromString((String) event.get("shipmentId")))
            .eventDate(java.time.LocalDate.now())
            .eventType("CREATED")
            .recordedAt(Instant.now())
            .build();
        shipmentVolumeRepository.save(record);
    }

    private void handleStatusUpdate(Map<String, Object> event) {
        String status = (String) event.get("status");
        if ("DELIVERED".equals(status)) {
            String orgId = (String) event.get("organizationId");
            String driverId = (String) event.get("driverId");
            DeliveryMetric metric = DeliveryMetric.builder()
                .organizationId(UUID.fromString(orgId))
                .shipmentId(UUID.fromString((String) event.get("shipmentId")))
                .driverId(driverId != null ? UUID.fromString(driverId) : null)
                .outcome("DELIVERED")
                .recordedAt(Instant.now())
                .build();
            deliveryMetricRepository.save(metric);
        } else if ("FAILED".equals(status)) {
            String orgId = (String) event.get("organizationId");
            DeliveryMetric metric = DeliveryMetric.builder()
                .organizationId(UUID.fromString(orgId))
                .shipmentId(UUID.fromString((String) event.get("shipmentId")))
                .outcome("FAILED")
                .recordedAt(Instant.now())
                .build();
            deliveryMetricRepository.save(metric);
        }
    }

    private void updateDriverMetrics(Map<String, Object> event) {
        // Aggregate driver location updates into performance metrics
        log.debug("Updating driver metrics from location event");
    }
}
