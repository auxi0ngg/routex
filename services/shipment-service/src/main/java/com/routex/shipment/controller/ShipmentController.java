package com.routex.shipment.controller;

import com.routex.shipment.dto.request.*;
import com.routex.shipment.dto.response.ShipmentResponse;
import com.routex.shipment.entity.ShipmentStatus;
import com.routex.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Shipment booking, tracking, and management")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @Operation(summary = "Create a new shipment")
    public ResponseEntity<ShipmentResponse> create(
            @Valid @RequestBody CreateShipmentRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Organization-Id") UUID orgId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(shipmentService.createShipment(request, userId, orgId));
    }

    @GetMapping
    @Operation(summary = "List shipments for an organization")
    public ResponseEntity<Page<ShipmentResponse>> list(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(shipmentService.getShipments(orgId, status, pageable));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number (public)")
    public ResponseEntity<ShipmentResponse> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getByTrackingNumber(trackingNumber));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentStatusRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, request, userId));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign driver and vehicle to shipment")
    public ResponseEntity<ShipmentResponse> assign(
            @PathVariable UUID id,
            @RequestParam UUID driverId,
            @RequestParam UUID vehicleId) {
        return ResponseEntity.ok(shipmentService.assignDriver(id, driverId, vehicleId));
    }

    @GetMapping("/driver/{driverId}/active")
    @Operation(summary = "Get active shipments for a driver")
    public ResponseEntity<?> driverActive(@PathVariable UUID driverId) {
        return ResponseEntity.ok(shipmentService.getDriverActiveShipments(driverId));
    }
}
