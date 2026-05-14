package com.routex.shipment.dto.response;
import com.routex.shipment.entity.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record ShipmentResponse(
    UUID id, String trackingNumber, ShipmentStatus status,
    UUID organizationId, UUID customerId, UUID assignedDriverId,
    String pickupCity, String pickupState,
    String deliveryCity, String deliveryState,
    BigDecimal weightKg, PackageType packageType,
    BigDecimal shippingCost, Instant scheduledDeliveryAt,
    Instant actualDeliveryAt, Instant estimatedDeliveryAt,
    boolean fragile, boolean requiresSignature,
    Instant createdAt, Instant updatedAt
) {}
