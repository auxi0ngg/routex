package com.routex.fleet.repository;

import com.routex.fleet.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    boolean existsByRegistrationNumber(String registrationNumber);
    Page<Vehicle> findByOrganizationId(UUID orgId, Pageable pageable);
    Page<Vehicle> findByOrganizationIdAndStatus(UUID orgId, VehicleStatus status, Pageable pageable);
    long countByOrganizationId(UUID orgId);
    long countByOrganizationIdAndStatus(UUID orgId, VehicleStatus status);
}
