package com.routex.shipment.dto.request;
import com.routex.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record UpdateShipmentStatusRequest(
    @NotNull ShipmentStatus newStatus,
    String remarks, BigDecimal latitude, BigDecimal longitude,
    String proofImageUrl
) {}
