package com.routex.analytics.repository;
import com.routex.analytics.entity.DeliveryMetric;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.*;

@Repository
public interface DeliveryMetricRepository extends JpaRepository<DeliveryMetric, UUID> {
    long countByOrganizationIdAndOutcomeAndRecordedAtBetween(UUID orgId, String outcome, Instant from, Instant to);
    long countByOrganizationIdAndRecordedAtBetween(UUID orgId, Instant from, Instant to);
    @Query("SELECT AVG(dm.deliveryDurationMinutes) FROM DeliveryMetric dm WHERE dm.organizationId=:orgId AND dm.recordedAt BETWEEN :from AND :to")
    Double avgDeliveryDuration(UUID orgId, Instant from, Instant to);
}
