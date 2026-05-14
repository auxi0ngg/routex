package com.routex.fleet.service;
import com.routex.fleet.dto.*;
import com.routex.fleet.entity.VehicleStatus;
import org.springframework.data.domain.*;
import java.util.UUID;
public interface VehicleService {
    VehicleResponse registerVehicle(CreateVehicleRequest request, UUID orgId);
    VehicleResponse assignDriver(UUID vehicleId, UUID driverId);
    VehicleResponse releaseDriver(UUID vehicleId);
    MaintenanceLogResponse logMaintenance(UUID vehicleId, CreateMaintenanceRequest req, UUID userId);
    Page<VehicleResponse> getOrgVehicles(UUID orgId, VehicleStatus status, Pageable pageable);
    FleetUtilizationStats getUtilizationStats(UUID orgId);
}
