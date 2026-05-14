package com.routex.analytics.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "delivery_metrics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryMetric {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID organizationId;
    private UUID shipmentId;
    private UUID driverId;
    @Column(length = 50) private String outcome;
    private Long deliveryDurationMinutes;
    @Column(precision = 10, scale = 2) private BigDecimal distanceKm;
    @Column(nullable = false) @Builder.Default private Instant recordedAt = Instant.now();
}
