package com.routex.analytics.controller;

import com.routex.analytics.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Operational analytics and KPI dashboards")
public class AnalyticsController {

    private final DeliveryMetricRepository deliveryMetricRepository;
    private final ShipmentVolumeRepository shipmentVolumeRepository;

    @GetMapping("/delivery-success-rate")
    @Operation(summary = "Get delivery success rate for a date range")
    public ResponseEntity<Map<String, Object>> deliverySuccessRate(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        long total = deliveryMetricRepository.countByOrganizationIdAndRecordedAtBetween(orgId, from, to);
        long delivered = deliveryMetricRepository.countByOrganizationIdAndOutcomeAndRecordedAtBetween(orgId, "DELIVERED", from, to);
        long failed = deliveryMetricRepository.countByOrganizationIdAndOutcomeAndRecordedAtBetween(orgId, "FAILED", from, to);
        double rate = total > 0 ? (double) delivered / total * 100 : 0;

        return ResponseEntity.ok(Map.of(
            "organizationId", orgId,
            "period", Map.of("from", from, "to", to),
            "totalDeliveries", total,
            "successful", delivered,
            "failed", failed,
            "successRate", Math.round(rate * 100.0) / 100.0
        ));
    }

    @GetMapping("/avg-delivery-time")
    @Operation(summary = "Get average delivery duration in minutes")
    public ResponseEntity<Map<String, Object>> avgDeliveryTime(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        Double avgMinutes = deliveryMetricRepository.avgDeliveryDuration(orgId, from, to);
        return ResponseEntity.ok(Map.of(
            "organizationId", orgId,
            "averageDeliveryTimeMinutes", avgMinutes != null ? Math.round(avgMinutes) : 0
        ));
    }

    @GetMapping("/shipment-volume")
    @Operation(summary = "Get daily shipment volume for the last N days")
    public ResponseEntity<Map<String, Object>> shipmentVolume(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(defaultValue = "30") int days) {

        LocalDate from = LocalDate.now().minusDays(days);
        List<Object[]> raw = shipmentVolumeRepository.getDailyVolume(orgId, from);

        List<Map<String, Object>> daily = raw.stream().map(row -> Map.of(
            "date", row[0].toString(),
            "count", row[1]
        )).toList();

        long total = daily.stream().mapToLong(m -> ((Number) m.get("count")).longValue()).sum();

        return ResponseEntity.ok(Map.of(
            "organizationId", orgId,
            "days", days,
            "totalShipments", total,
            "daily", daily
        ));
    }

    @GetMapping("/dashboard-summary")
    @Operation(summary = "Get complete dashboard summary for today")
    public ResponseEntity<Map<String, Object>> dashboardSummary(
            @RequestHeader("X-Organization-Id") UUID orgId) {

        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant now = Instant.now();

        long todayTotal = deliveryMetricRepository.countByOrganizationIdAndRecordedAtBetween(orgId, startOfDay, now);
        long todayDelivered = deliveryMetricRepository.countByOrganizationIdAndOutcomeAndRecordedAtBetween(orgId, "DELIVERED", startOfDay, now);
        long todayFailed = deliveryMetricRepository.countByOrganizationIdAndOutcomeAndRecordedAtBetween(orgId, "FAILED", startOfDay, now);
        double successRate = todayTotal > 0 ? (double) todayDelivered / todayTotal * 100 : 0;
        Double avgTime = deliveryMetricRepository.avgDeliveryDuration(orgId, startOfDay, now);
        long volumeToday = shipmentVolumeRepository.countByOrganizationIdAndEventDateBetween(orgId, LocalDate.now(), LocalDate.now());

        return ResponseEntity.ok(Map.of(
            "date", LocalDate.now(),
            "shipmentsToday", volumeToday,
            "deliveriesToday", todayTotal,
            "successRate", Math.round(successRate * 100.0) / 100.0,
            "failedDeliveries", todayFailed,
            "avgDeliveryMinutes", avgTime != null ? Math.round(avgTime) : 0
        ));
    }
}
