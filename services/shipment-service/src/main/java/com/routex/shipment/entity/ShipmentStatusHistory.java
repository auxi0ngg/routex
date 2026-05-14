package com.routex.shipment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(length = 500)
    private String remarks;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
    private UUID updatedByUserId;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
