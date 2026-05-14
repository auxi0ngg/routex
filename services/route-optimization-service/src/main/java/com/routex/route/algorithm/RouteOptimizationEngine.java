package com.routex.route.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Graph-based route optimization using Dijkstra and A* algorithms.
 * Supports multi-stop optimization using nearest-neighbor heuristic.
 */
@Component
@Slf4j
public class RouteOptimizationEngine {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_SPEED_KMH = 40.0; // urban average
    private static final double TRAFFIC_FACTOR = 1.3;    // 30% traffic overhead

    /**
     * Optimize multi-stop delivery route using nearest-neighbor heuristic.
     * Complexity: O(n²) — suitable for n < 50 stops (typical delivery batch).
     */
    public OptimizedRoute optimizeMultiStop(GeoPoint origin, List<GeoPoint> stops, GeoPoint depot) {
        if (stops.isEmpty()) {
            return OptimizedRoute.builder()
                .orderedStops(List.of())
                .totalDistanceKm(0)
                .estimatedDurationMinutes(0)
                .build();
        }

        List<GeoPoint> remaining = new ArrayList<>(stops);
        List<GeoPoint> ordered = new ArrayList<>();
        GeoPoint current = origin;
        double totalDistance = 0;

        // Nearest-neighbor greedy construction
        while (!remaining.isEmpty()) {
            GeoPoint nearest = findNearest(current, remaining);
            double dist = haversineDistance(current, nearest);
            totalDistance += dist;
            ordered.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        // Add return to depot
        if (depot != null) {
            totalDistance += haversineDistance(current, depot);
        }

        // Apply 2-opt local search improvement
        ordered = twoOptImprove(ordered, origin);

        double durationMinutes = (totalDistance / AVERAGE_SPEED_KMH) * 60 * TRAFFIC_FACTOR;

        return OptimizedRoute.builder()
            .orderedStops(ordered)
            .totalDistanceKm(Math.round(totalDistance * 100.0) / 100.0)
            .estimatedDurationMinutes((long) Math.ceil(durationMinutes))
            .build();
    }

    /**
     * A* algorithm for point-to-point shortest path.
     * Uses haversine distance as heuristic.
     */
    public PathResult aStarPath(GeoPoint start, GeoPoint goal, List<GeoPoint> waypoints) {
        // Build graph from waypoints
        List<GeoPoint> allPoints = new ArrayList<>();
        allPoints.add(start);
        allPoints.addAll(waypoints);
        allPoints.add(goal);

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<GeoPoint, Double> gScore = new HashMap<>();
        Map<GeoPoint, GeoPoint> cameFrom = new HashMap<>();

        gScore.put(start, 0.0);
        openSet.offer(new AStarNode(start, 0, haversineDistance(start, goal)));

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            if (isClose(current.point, goal)) {
                return reconstructPath(cameFrom, current.point, gScore.getOrDefault(current.point, 0.0));
            }

            // Explore neighbors (all reachable points in graph)
            for (GeoPoint neighbor : allPoints) {
                if (neighbor.equals(current.point)) continue;
                double edgeDist = haversineDistance(current.point, neighbor);
                double tentativeG = gScore.getOrDefault(current.point, Double.MAX_VALUE) + edgeDist;

                if (tentativeG < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.point);
                    gScore.put(neighbor, tentativeG);
                    double h = haversineDistance(neighbor, goal);
                    openSet.offer(new AStarNode(neighbor, tentativeG, tentativeG + h));
                }
            }
        }

        // Fallback: direct path
        double directDist = haversineDistance(start, goal);
        return PathResult.builder()
            .path(List.of(start, goal))
            .distanceKm(directDist)
            .durationMinutes((long) ((directDist / AVERAGE_SPEED_KMH) * 60 * TRAFFIC_FACTOR))
            .build();
    }

    /**
     * Find nearest available driver to a shipment pickup location.
     */
    public DriverAssignment findNearestDriver(GeoPoint pickupLocation, List<DriverLocation> availableDrivers) {
        return availableDrivers.stream()
            .min(Comparator.comparingDouble(d ->
                haversineDistance(pickupLocation, new GeoPoint(d.latitude(), d.longitude()))))
            .map(d -> new DriverAssignment(
                d.driverId(),
                haversineDistance(pickupLocation, new GeoPoint(d.latitude(), d.longitude())),
                estimateEtaMinutes(pickupLocation, new GeoPoint(d.latitude(), d.longitude()))
            ))
            .orElse(null);
    }

    /**
     * Calculate ETA in minutes using haversine distance + traffic factor.
     */
    public long estimateEtaMinutes(GeoPoint from, GeoPoint to) {
        double dist = haversineDistance(from, to);
        return (long) Math.ceil((dist / AVERAGE_SPEED_KMH) * 60 * TRAFFIC_FACTOR);
    }

    // ─── Haversine distance formula ────────────────────────────────────────────

    public double haversineDistance(GeoPoint a, GeoPoint b) {
        double lat1 = Math.toRadians(a.latitude());
        double lat2 = Math.toRadians(b.latitude());
        double dLat = Math.toRadians(b.latitude() - a.latitude());
        double dLon = Math.toRadians(b.longitude() - a.longitude());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    // ─── 2-opt local search improvement ───────────────────────────────────────

    private List<GeoPoint> twoOptImprove(List<GeoPoint> route, GeoPoint origin) {
        List<GeoPoint> best = new ArrayList<>(route);
        boolean improved = true;
        int iterations = 0;

        while (improved && iterations < 100) {
            improved = false;
            iterations++;
            for (int i = 1; i < best.size() - 1; i++) {
                for (int j = i + 1; j < best.size(); j++) {
                    double currentDist = segmentDist(best, i - 1, i) + segmentDist(best, j, j < best.size() - 1 ? j + 1 : 0);
                    double newDist = segmentDist(best, i - 1, j) + segmentDist(best, i, j < best.size() - 1 ? j + 1 : 0);
                    if (newDist < currentDist - 1e-9) {
                        reverseSegment(best, i, j);
                        improved = true;
                    }
                }
            }
        }
        log.debug("2-opt completed in {} iterations", iterations);
        return best;
    }

    private double segmentDist(List<GeoPoint> route, int i, int j) {
        if (i < 0 || i >= route.size() || j < 0 || j >= route.size()) return 0;
        return haversineDistance(route.get(i), route.get(j));
    }

    private void reverseSegment(List<GeoPoint> route, int i, int j) {
        while (i < j) {
            GeoPoint tmp = route.get(i);
            route.set(i, route.get(j));
            route.set(j, tmp);
            i++; j--;
        }
    }

    private GeoPoint findNearest(GeoPoint current, List<GeoPoint> candidates) {
        return candidates.stream()
            .min(Comparator.comparingDouble(p -> haversineDistance(current, p)))
            .orElseThrow();
    }

    private boolean isClose(GeoPoint a, GeoPoint b) {
        return haversineDistance(a, b) < 0.01; // within 10m
    }

    private PathResult reconstructPath(Map<GeoPoint, GeoPoint> cameFrom, GeoPoint current, double distKm) {
        List<GeoPoint> path = new ArrayList<>();
        GeoPoint node = current;
        while (node != null) {
            path.add(0, node);
            node = cameFrom.get(node);
        }
        double duration = (distKm / AVERAGE_SPEED_KMH) * 60 * TRAFFIC_FACTOR;
        return PathResult.builder()
            .path(path)
            .distanceKm(Math.round(distKm * 100.0) / 100.0)
            .durationMinutes((long) Math.ceil(duration))
            .build();
    }

    // ─── Inner types ───────────────────────────────────────────────────────────

    private record AStarNode(GeoPoint point, double gScore, double fScore) {}

    public record GeoPoint(double latitude, double longitude) {}

    public record DriverLocation(String driverId, double latitude, double longitude) {}

    public record DriverAssignment(String driverId, double distanceKm, long etaMinutes) {}

    @lombok.Builder
    public record OptimizedRoute(List<GeoPoint> orderedStops, double totalDistanceKm, long estimatedDurationMinutes) {}

    @lombok.Builder
    public record PathResult(List<GeoPoint> path, double distanceKm, long durationMinutes) {}
}
