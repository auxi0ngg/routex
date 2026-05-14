package com.routex.analytics.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "shipment_volume_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentVolumeRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    private UUID organizationId;
    private UUID shipmentId;
    private LocalDate eventDate;
    @Column(length = 50) private String eventType;
    @Column(nullable = false) @Builder.Default private Instant recordedAt = Instant.now();
}
