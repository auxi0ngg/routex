package com.routex.driver.entity;

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
@Table(name = "drivers", indexes = {
    @Index(name = "idx_drivers_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_drivers_license", columnList = "license_number", unique = true),
    @Index(name = "idx_drivers_status", columnList = "status"),
    @Index(name = "idx_drivers_org", columnList = "organization_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(unique = true, length = 30)
    private String licenseNumber;

    private LocalDate licenseExpiryDate;

    @Column(length = 50)
    private String licenseClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.OFFLINE;

    // Current assignment
    private UUID currentVehicleId;
    private UUID currentShipmentId;

    // Performance metrics
    @Column(precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Builder.Default
    private Integer totalDeliveries = 0;

    @Builder.Default
    private Integer successfulDeliveries = 0;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    // Emergency contact
    @Column(length = 100)
    private String emergencyContactName;

    @Column(length = 20)
    private String emergencyContactPhone;

    // Address
    @Column(length = 500)
    private String homeAddress;

    // Documents
    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 500)
    private String licenseImageUrl;

    @Column(length = 500)
    private String aadhaarImageUrl;

    @Builder.Default
    private boolean backgroundVerified = false;

    private Instant verifiedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public String getFullName() { return firstName + " " + lastName; }

    public double getDeliverySuccessRate() {
        return totalDeliveries > 0 ? (double) successfulDeliveries / totalDeliveries * 100 : 0;
    }
}
