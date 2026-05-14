package com.routex.shipment.service;
import com.routex.shipment.dto.request.*;
import com.routex.shipment.dto.response.ShipmentResponse;
import com.routex.shipment.entity.ShipmentStatus;
import org.springframework.data.domain.*;
import java.util.*;
public interface ShipmentService {
    ShipmentResponse createShipment(CreateShipmentRequest request, UUID customerId, UUID orgId);
    ShipmentResponse updateStatus(UUID id, UpdateShipmentStatusRequest req, UUID updatedBy);
    ShipmentResponse assignDriver(UUID id, UUID driverId, UUID vehicleId);
    ShipmentResponse getByTrackingNumber(String trackingNumber);
    Page<ShipmentResponse> getShipments(UUID orgId, ShipmentStatus status, Pageable pageable);
    List<ShipmentResponse> getDriverActiveShipments(UUID driverId);
}
