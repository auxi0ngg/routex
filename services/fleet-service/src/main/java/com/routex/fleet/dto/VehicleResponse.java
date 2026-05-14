package com.routex.fleet.dto;
import com.routex.fleet.entity.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
public record VehicleResponse(UUID id, String registrationNumber, VehicleType type, String make,
    String model, Integer year, VehicleStatus status, UUID assignedDriverId,
    BigDecimal payloadCapacityKg, FuelType fuelType, BigDecimal odometerKm,
    LocalDate lastMaintenanceDate, LocalDate nextMaintenanceDue,
    LocalDate insuranceExpiryDate, Instant createdAt) {}
