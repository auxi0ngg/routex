package com.routex.fleet.controller;

import com.routex.fleet.dto.*;
import com.routex.fleet.entity.VehicleStatus;
import com.routex.fleet.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Fleet Management", description = "Vehicle registration, maintenance, and assignment APIs")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Register a new vehicle")
    public ResponseEntity<VehicleResponse> register(
            @Valid @RequestBody CreateVehicleRequest request,
            @RequestHeader("X-Organization-Id") UUID orgId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(vehicleService.registerVehicle(request, orgId));
    }

    @GetMapping
    @Operation(summary = "List all vehicles for organization")
    public ResponseEntity<Page<VehicleResponse>> list(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(vehicleService.getOrgVehicles(orgId, status, pageable));
    }

    @PostMapping("/{vehicleId}/assign/{driverId}")
    @Operation(summary = "Assign driver to vehicle")
    public ResponseEntity<VehicleResponse> assign(
            @PathVariable UUID vehicleId, @PathVariable UUID driverId) {
        return ResponseEntity.ok(vehicleService.assignDriver(vehicleId, driverId));
    }

    @PostMapping("/{vehicleId}/release")
    @Operation(summary = "Release vehicle from driver")
    public ResponseEntity<VehicleResponse> release(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(vehicleService.releaseDriver(vehicleId));
    }

    @PostMapping("/{vehicleId}/maintenance")
    @Operation(summary = "Log a maintenance record")
    public ResponseEntity<MaintenanceLogResponse> logMaintenance(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateMaintenanceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(vehicleService.logMaintenance(vehicleId, request, userId));
    }

    @GetMapping("/stats/utilization")
    @Operation(summary = "Get fleet utilization statistics")
    public ResponseEntity<FleetUtilizationStats> utilization(
            @RequestHeader("X-Organization-Id") UUID orgId) {
        return ResponseEntity.ok(vehicleService.getUtilizationStats(orgId));
    }
}
