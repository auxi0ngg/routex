package com.routex.fleet.dto;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
public record MaintenanceLogResponse(UUID id, UUID vehicleId, LocalDate serviceDate,
    String serviceType, String description, BigDecimal costAmount,
    String vendorName, Instant createdAt) {}
