package com.routex.fleet.service.impl;

import com.routex.fleet.dto.*;
import com.routex.fleet.entity.*;
import com.routex.fleet.exception.*;
import com.routex.fleet.repository.*;
import com.routex.fleet.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public VehicleResponse registerVehicle(CreateVehicleRequest request, UUID orgId) {
        if (vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new VehicleAlreadyExistsException(
                "Vehicle already registered: " + request.registrationNumber());
        }

        Vehicle vehicle = Vehicle.builder()
            .organizationId(orgId)
            .registrationNumber(request.registrationNumber().toUpperCase())
            .type(request.type())
            .make(request.make())
            .model(request.model())
            .year(request.year())
            .color(request.color())
            .payloadCapacityKg(request.payloadCapacityKg())
            .volumeCapacityM3(request.volumeCapacityM3())
            .fuelType(request.fuelType())
            .fuelTankCapacityL(request.fuelTankCapacityL())
            .fuelEfficiencyKmpl(request.fuelEfficiencyKmpl())
            .odometerKm(request.odometerKm())
            .gpsDeviceId(request.gpsDeviceId())
            .insuranceExpiryDate(request.insuranceExpiryDate())
            .pollutionCertExpiry(request.pollutionCertExpiry())
            .permitExpiry(request.permitExpiry())
            .status(VehicleStatus.AVAILABLE)
            .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Registered vehicle: {} for org: {}", vehicle.getRegistrationNumber(), orgId);

        publishVehicleEvent("VEHICLE_REGISTERED", vehicle);
        return toResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse assignDriver(UUID vehicleId, UUID driverId) {
        Vehicle vehicle = findById(vehicleId);
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("Vehicle is not available: " + vehicleId);
        }
        vehicle.setAssignedDriverId(driverId);
        vehicle.setStatus(VehicleStatus.IN_USE);
        vehicle = vehicleRepository.save(vehicle);
        publishVehicleEvent("VEHICLE_ASSIGNED", vehicle);
        return toResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse releaseDriver(UUID vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setAssignedDriverId(null);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);
        publishVehicleEvent("VEHICLE_RELEASED", vehicle);
        return toResponse(vehicle);
    }

    @Override
    @Transactional
    public MaintenanceLogResponse logMaintenance(UUID vehicleId, CreateMaintenanceRequest req, UUID userId) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setLastMaintenanceDate(req.serviceDate());
        vehicle.setNextMaintenanceDue(req.nextMaintenanceDue());
        if (req.sendForMaintenance()) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
        }
        vehicleRepository.save(vehicle);

        MaintenanceLog log = MaintenanceLog.builder()
            .vehicle(vehicle)
            .serviceDate(req.serviceDate())
            .serviceType(req.serviceType())
            .description(req.description())
            .costAmount(req.costAmount())
            .currency(req.currency() != null ? req.currency() : "INR")
            .vendorName(req.vendorName())
            .odometerAtService(req.odometerAtService())
            .invoiceNumber(req.invoiceNumber())
            .recordedByUserId(userId)
            .build();

        log = maintenanceLogRepository.save(log);
        return toMaintenanceResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getOrgVehicles(UUID orgId, VehicleStatus status, Pageable pageable) {
        Page<Vehicle> page = (status != null)
            ? vehicleRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
            : vehicleRepository.findByOrganizationId(orgId, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FleetUtilizationStats getUtilizationStats(UUID orgId) {
        long total = vehicleRepository.countByOrganizationId(orgId);
        long inUse = vehicleRepository.countByOrganizationIdAndStatus(orgId, VehicleStatus.IN_USE);
        long maintenance = vehicleRepository.countByOrganizationIdAndStatus(orgId, VehicleStatus.MAINTENANCE);
        long available = vehicleRepository.countByOrganizationIdAndStatus(orgId, VehicleStatus.AVAILABLE);
        double utilization = total > 0 ? (double) inUse / total * 100 : 0;
        return new FleetUtilizationStats(total, inUse, available, maintenance,
            Math.round(utilization * 100.0) / 100.0);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Vehicle findById(UUID id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + id));
    }

    private void publishVehicleEvent(String eventType, Vehicle vehicle) {
        Map<String, Object> event = Map.of(
            "eventType", eventType,
            "vehicleId", vehicle.getId().toString(),
            "registrationNumber", vehicle.getRegistrationNumber(),
            "status", vehicle.getStatus().name(),
            "organizationId", vehicle.getOrganizationId().toString(),
            "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send("fleet-events", vehicle.getId().toString(), event);
    }

    private VehicleResponse toResponse(Vehicle v) {
        return new VehicleResponse(
            v.getId(), v.getRegistrationNumber(), v.getType(), v.getMake(),
            v.getModel(), v.getYear(), v.getStatus(), v.getAssignedDriverId(),
            v.getPayloadCapacityKg(), v.getFuelType(), v.getOdometerKm(),
            v.getLastMaintenanceDate(), v.getNextMaintenanceDue(),
            v.getInsuranceExpiryDate(), v.getCreatedAt()
        );
    }

    private MaintenanceLogResponse toMaintenanceResponse(MaintenanceLog m) {
        return new MaintenanceLogResponse(
            m.getId(), m.getVehicle().getId(), m.getServiceDate(),
            m.getServiceType(), m.getDescription(), m.getCostAmount(),
            m.getVendorName(), m.getCreatedAt()
        );
    }
}
