package com.routex.tracking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracking_events", indexes = {
    @Index(name = "idx_tracking_driver_time", columnList = "driver_id, created_at"),
    @Index(name = "idx_tracking_shipment_time", columnList = "shipment_id, created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID driverId;
    private UUID shipmentId;
    @Column(precision = 10, scale = 8) private BigDecimal latitude;
    @Column(precision = 11, scale = 8) private BigDecimal longitude;
    private Double heading;
    @Column(precision = 8, scale = 2) private BigDecimal speedKmh;
    private Double accuracy;
    @Column(length = 50) private String eventType;
    @Column(nullable = false) @Builder.Default private Instant createdAt = Instant.now();
}
