package com.routex.shipment.service.impl;

import com.routex.shipment.dto.request.*;
import com.routex.shipment.dto.response.*;
import com.routex.shipment.entity.*;
import com.routex.shipment.exception.*;
import com.routex.shipment.kafka.producer.ShipmentEventProducer;
import com.routex.shipment.mapper.ShipmentMapper;
import com.routex.shipment.repository.*;
import com.routex.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentEventProducer eventProducer;

    @Override
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request, UUID customerId, UUID orgId) {
        Shipment shipment = Shipment.builder()
                .trackingNumber(Shipment.generateTrackingNumber())
                .organizationId(orgId)
                .customerId(customerId)
                .pickupAddress(shipmentMapper.toAddress(request.pickupAddress()))
                .deliveryAddress(shipmentMapper.toAddress(request.deliveryAddress()))
                .weightKg(request.weightKg())
                .lengthCm(request.lengthCm())
                .widthCm(request.widthCm())
                .heightCm(request.heightCm())
                .packageType(request.packageType())
                .packageDescription(request.packageDescription())
                .declaredValue(request.declaredValue())
                .shippingCost(request.shippingCost())
                .scheduledPickupAt(request.scheduledPickupAt())
                .scheduledDeliveryAt(request.scheduledDeliveryAt())
                .fragile(request.fragile())
                .requiresSignature(request.requiresSignature())
                .cashOnDelivery(request.cashOnDelivery())
                .codAmount(request.codAmount())
                .priority(request.priority())
                .status(ShipmentStatus.CREATED)
                .build();

        shipment = shipmentRepository.save(shipment);

        // Add initial status history
        addStatusHistory(shipment, ShipmentStatus.CREATED, "Shipment created", null, null, customerId);

        log.info("Created shipment: {} for org: {}", shipment.getTrackingNumber(), orgId);
        eventProducer.publishShipmentCreated(shipment);

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(UUID shipmentId, UpdateShipmentStatusRequest request, UUID updatedBy) {
        Shipment shipment = findById(shipmentId);

        validateStatusTransition(shipment.getStatus(), request.newStatus());

        ShipmentStatus oldStatus = shipment.getStatus();
        shipment.setStatus(request.newStatus());

        // Set timestamps based on status
        switch (request.newStatus()) {
            case PICKED_UP -> shipment.setActualPickupAt(Instant.now());
            case DELIVERED -> {
                shipment.setActualDeliveryAt(Instant.now());
                shipment.setProofOfDeliveryImageUrl(request.proofImageUrl());
                shipment.setDeliveryNotes(request.remarks());
            }
            default -> {}
        }

        addStatusHistory(shipment, request.newStatus(), request.remarks(),
                request.latitude(), request.longitude(), updatedBy);

        shipment = shipmentRepository.save(shipment);

        log.info("Shipment {} status: {} -> {}", shipment.getTrackingNumber(), oldStatus, request.newStatus());
        eventProducer.publishStatusUpdated(shipment, oldStatus);

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse assignDriver(UUID shipmentId, UUID driverId, UUID vehicleId) {
        Shipment shipment = findById(shipmentId);
        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new InvalidStatusTransitionException("Can only assign driver to CREATED shipments");
        }
        shipment.setAssignedDriverId(driverId);
        shipment.setAssignedVehicleId(vehicleId);
        shipment.setStatus(ShipmentStatus.ASSIGNED);
        addStatusHistory(shipment, ShipmentStatus.ASSIGNED, "Driver assigned", null, null, driverId);
        shipment = shipmentRepository.save(shipment);
        eventProducer.publishShipmentAssigned(shipment);
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getByTrackingNumber(String trackingNumber) {
        return shipmentMapper.toResponse(
            shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + trackingNumber))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipments(UUID orgId, ShipmentStatus status, Pageable pageable) {
        Page<Shipment> page = (status != null)
            ? shipmentRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
            : shipmentRepository.findByOrganizationId(orgId, pageable);
        return page.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getDriverActiveShipments(UUID driverId) {
        return shipmentRepository.findActiveByDriver(driverId)
                .stream().map(shipmentMapper::toResponse).toList();
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Shipment findById(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + id));
    }

    private void addStatusHistory(Shipment shipment, ShipmentStatus status, String remarks,
                                   java.math.BigDecimal lat, java.math.BigDecimal lon, UUID updatedBy) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(shipment)
                .status(status)
                .remarks(remarks)
                .latitude(lat)
                .longitude(lon)
                .updatedByUserId(updatedBy)
                .build();
        shipment.getStatusHistory().add(history);
    }

    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        boolean valid = switch (current) {
            case CREATED -> next == ShipmentStatus.ASSIGNED || next == ShipmentStatus.CANCELLED;
            case ASSIGNED -> next == ShipmentStatus.PICKED_UP || next == ShipmentStatus.CANCELLED;
            case PICKED_UP -> next == ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> next == ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == ShipmentStatus.DELIVERED || next == ShipmentStatus.FAILED;
            case FAILED -> next == ShipmentStatus.RETURNED || next == ShipmentStatus.ASSIGNED;
            default -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot transition from %s to %s", current, next));
        }
    }
}
