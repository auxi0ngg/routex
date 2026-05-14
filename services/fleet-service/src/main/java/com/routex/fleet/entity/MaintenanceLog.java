package com.routex.fleet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_logs", indexes = {
    @Index(name = "idx_maintenance_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_maintenance_date", columnList = "service_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false, length = 50)
    private String serviceType; // ROUTINE, REPAIR, EMERGENCY, INSPECTION

    @Column(length = 1000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal costAmount;

    @Column(length = 100)
    private String currency;

    @Column(length = 255)
    private String vendorName;

    @Column(precision = 12, scale = 2)
    private BigDecimal odometerAtService;

    @Column(length = 255)
    private String invoiceNumber;

    @Column(length = 500)
    private String invoiceUrl;

    private UUID recordedByUserId;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
