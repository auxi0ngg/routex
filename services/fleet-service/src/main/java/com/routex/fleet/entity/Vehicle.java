package com.routex.fleet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicles_registration", columnList = "registration_number", unique = true),
    @Index(name = "idx_vehicles_org", columnList = "organization_id"),
    @Index(name = "idx_vehicles_status", columnList = "status"),
    @Index(name = "idx_vehicles_driver", columnList = "assigned_driver_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false, length = 100)
    private String make;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    // Capacity
    @Column(precision = 10, scale = 3)
    private BigDecimal payloadCapacityKg;

    @Column(precision = 8, scale = 2)
    private BigDecimal volumeCapacityM3;

    // Fuel
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Column(precision = 8, scale = 2)
    private BigDecimal fuelTankCapacityL;

    @Column(precision = 8, scale = 2)
    private BigDecimal currentFuelLevelL;

    @Column(precision = 8, scale = 4)
    private BigDecimal fuelEfficiencyKmpl;

    // Odometer
    @Column(precision = 12, scale = 2)
    private BigDecimal odometerKm;

    // Assignment
    private UUID assignedDriverId;

    // Maintenance
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDue;
    private BigDecimal nextMaintenanceOdometerKm;

    // Insurance / Permits
    private LocalDate insuranceExpiryDate;
    private LocalDate pollutionCertExpiry;
    private LocalDate permitExpiry;

    // GPS Device
    @Column(length = 100)
    private String gpsDeviceId;

    // Documents
    @Column(length = 500)
    private String rcBookUrl;

    @Column(length = 500)
    private String insuranceDocUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
