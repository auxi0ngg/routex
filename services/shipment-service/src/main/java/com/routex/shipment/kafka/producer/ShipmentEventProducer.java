package com.routex.shipment.kafka.producer;

import com.routex.shipment.entity.Shipment;
import com.routex.shipment.entity.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishShipmentCreated(Shipment shipment) {
        send("shipment-created", shipment.getId().toString(), buildEvent("SHIPMENT_CREATED", shipment));
    }

    public void publishShipmentAssigned(Shipment shipment) {
        send("shipment-assigned", shipment.getId().toString(), buildEvent("SHIPMENT_ASSIGNED", shipment));
    }

    public void publishStatusUpdated(Shipment shipment, ShipmentStatus oldStatus) {
        Map<String, Object> event = buildEvent("SHIPMENT_STATUS_UPDATED", shipment);
        event.put("previousStatus", oldStatus.name());
        send("tracking-events", shipment.getId().toString(), event);
        send("notification-events", shipment.getId().toString(), event);
        send("analytics-events", shipment.getId().toString(), event);
    }

    private Map<String, Object> buildEvent(String eventType, Shipment shipment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("shipmentId", shipment.getId().toString());
        event.put("trackingNumber", shipment.getTrackingNumber());
        event.put("status", shipment.getStatus().name());
        event.put("organizationId", shipment.getOrganizationId().toString());
        event.put("customerId", shipment.getCustomerId().toString());
        if (shipment.getAssignedDriverId() != null) {
            event.put("driverId", shipment.getAssignedDriverId().toString());
        }
        event.put("timestamp", Instant.now().toString());
        return event;
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("Kafka send failed [{}]: {}", topic, ex.getMessage());
                else log.debug("Sent to {}", topic);
            });
    }
}
