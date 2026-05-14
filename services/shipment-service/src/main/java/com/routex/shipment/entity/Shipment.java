package com.routex.shipment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipments_tracking_number", columnList = "tracking_number", unique = true),
    @Index(name = "idx_shipments_status", columnList = "status"),
    @Index(name = "idx_shipments_org", columnList = "organization_id"),
    @Index(name = "idx_shipments_driver", columnList = "assigned_driver_id"),
    @Index(name = "idx_shipments_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String trackingNumber;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private UUID customerId;

    // Pickup details
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "addressLine1", column = @Column(name = "pickup_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "pickup_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "pickup_city")),
        @AttributeOverride(name = "state", column = @Column(name = "pickup_state")),
        @AttributeOverride(name = "pincode", column = @Column(name = "pickup_pincode")),
        @AttributeOverride(name = "country", column = @Column(name = "pickup_country")),
        @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude", precision = 10, scale = 8)),
        @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude", precision = 11, scale = 8)),
        @AttributeOverride(name = "contactName", column = @Column(name = "pickup_contact_name")),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "pickup_contact_phone"))
    })
    private Address pickupAddress;

    // Delivery details
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "addressLine1", column = @Column(name = "delivery_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "delivery_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "delivery_city")),
        @AttributeOverride(name = "state", column = @Column(name = "delivery_state")),
        @AttributeOverride(name = "pincode", column = @Column(name = "delivery_pincode")),
        @AttributeOverride(name = "country", column = @Column(name = "delivery_country")),
        @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude", precision = 10, scale = 8)),
        @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude", precision = 11, scale = 8)),
        @AttributeOverride(name = "contactName", column = @Column(name = "delivery_contact_name")),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "delivery_contact_phone"))
    })
    private Address deliveryAddress;

    // Package details
    @Column(precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(precision = 8, scale = 2)
    private BigDecimal lengthCm;

    @Column(precision = 8, scale = 2)
    private BigDecimal widthCm;

    @Column(precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PackageType packageType = PackageType.PARCEL;

    @Column(length = 500)
    private String packageDescription;

    @Column(precision = 12, scale = 2)
    private BigDecimal declaredValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingCost;

    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    // Assignments
    private UUID assignedDriverId;
    private UUID assignedVehicleId;
    private UUID warehouseId;

    // Scheduling
    private Instant scheduledPickupAt;
    private Instant scheduledDeliveryAt;
    private Instant actualPickupAt;
    private Instant actualDeliveryAt;
    private Instant estimatedDeliveryAt;

    // Proof of delivery
    @Column(length = 1000)
    private String proofOfDeliveryImageUrl;

    @Column(length = 500)
    private String deliverySignatureUrl;

    @Column(length = 255)
    private String deliveryNotes;

    // Special handling
    @Builder.Default
    private boolean fragile = false;

    @Builder.Default
    private boolean requiresSignature = false;

    @Builder.Default
    private boolean cashOnDelivery = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal codAmount;

    @Column(length = 100)
    private String priority;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShipmentStatusHistory> statusHistory = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Business method: generate tracking number
    public static String generateTrackingNumber() {
        return "RTX" + System.currentTimeMillis() % 1_000_000_000L +
               String.format("%04d", (int)(Math.random() * 9999));
    }
}
