package com.routex.shipment.repository;

import com.routex.shipment.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Page<Shipment> findByOrganizationId(UUID orgId, Pageable pageable);
    Page<Shipment> findByOrganizationIdAndStatus(UUID orgId, ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.assignedDriverId = :driverId AND s.status IN ('ASSIGNED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY')")
    List<Shipment> findActiveByDriver(UUID driverId);

    long countByOrganizationIdAndStatus(UUID orgId, ShipmentStatus status);
}
