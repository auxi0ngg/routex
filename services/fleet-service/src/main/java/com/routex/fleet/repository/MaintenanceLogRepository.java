package com.routex.fleet.repository;
import com.routex.fleet.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, UUID> {
    List<MaintenanceLog> findByVehicleIdOrderByServiceDateDesc(UUID vehicleId);
}
