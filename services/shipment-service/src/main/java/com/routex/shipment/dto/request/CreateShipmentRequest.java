package com.routex.shipment.dto.request;

import com.routex.shipment.entity.PackageType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateShipmentRequest(
    @NotNull AddressDto pickupAddress,
    @NotNull AddressDto deliveryAddress,
    @Positive BigDecimal weightKg,
    @Positive BigDecimal lengthCm,
    @Positive BigDecimal widthCm,
    @Positive BigDecimal heightCm,
    PackageType packageType,
    @Size(max = 500) String packageDescription,
    @Positive BigDecimal declaredValue,
    @Positive BigDecimal shippingCost,
    Instant scheduledPickupAt,
    Instant scheduledDeliveryAt,
    boolean fragile,
    boolean requiresSignature,
    boolean cashOnDelivery,
    @Positive BigDecimal codAmount,
    String priority
) {}
