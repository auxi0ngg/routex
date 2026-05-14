package com.routex.fleet.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record CreateMaintenanceRequest(
    @NotNull LocalDate serviceDate, @NotBlank String serviceType,
    String description, BigDecimal costAmount, String currency,
    String vendorName, BigDecimal odometerAtService,
    String invoiceNumber, LocalDate nextMaintenanceDue,
    boolean sendForMaintenance) {}
