package com.routex.analytics.repository;
import com.routex.analytics.entity.ShipmentVolumeRecord;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.*;

@Repository
public interface ShipmentVolumeRepository extends JpaRepository<ShipmentVolumeRecord, UUID> {
    long countByOrganizationIdAndEventDateBetween(UUID orgId, LocalDate from, LocalDate to);
    @Query("SELECT svr.eventDate, COUNT(svr) FROM ShipmentVolumeRecord svr WHERE svr.organizationId=:orgId AND svr.eventDate >= :from GROUP BY svr.eventDate ORDER BY svr.eventDate")
    List<Object[]> getDailyVolume(UUID orgId, LocalDate from);
}
