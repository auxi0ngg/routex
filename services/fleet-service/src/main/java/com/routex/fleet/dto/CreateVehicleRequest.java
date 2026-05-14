package com.routex.fleet.dto;

import com.routex.fleet.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreateVehicleRequest(
    @NotBlank @Size(max = 20) String registrationNumber,
    @NotNull VehicleType type,
    @NotBlank String make,
    @NotBlank String model,
    @NotNull @Min(1990) @Max(2030) Integer year,
    String color,
    @Positive BigDecimal payloadCapacityKg,
    @Positive BigDecimal volumeCapacityM3,
    FuelType fuelType,
    @Positive BigDecimal fuelTankCapacityL,
    @Positive BigDecimal fuelEfficiencyKmpl,
    @PositiveOrZero BigDecimal odometerKm,
    String gpsDeviceId,
    LocalDate insuranceExpiryDate,
    LocalDate pollutionCertExpiry,
    LocalDate permitExpiry
) {}
