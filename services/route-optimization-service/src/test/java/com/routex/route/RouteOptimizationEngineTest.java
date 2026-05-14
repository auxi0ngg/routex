package com.routex.route.algorithm;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

@DisplayName("RouteOptimizationEngine Tests")
class RouteOptimizationEngineTest {

    private RouteOptimizationEngine engine;

    @BeforeEach
    void setUp() { engine = new RouteOptimizationEngine(); }

    @Test
    @DisplayName("Haversine distance between same point should be zero")
    void testHaversineZeroDistance() {
        var point = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        double dist = engine.haversineDistance(point, point);
        assertThat(dist).isLessThan(0.001);
    }

    @Test
    @DisplayName("Haversine distance Delhi to Mumbai should be ~1150 km")
    void testHaversineDelhibToMumbai() {
        var delhi = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var mumbai = new RouteOptimizationEngine.GeoPoint(19.0760, 72.8777);
        double dist = engine.haversineDistance(delhi, mumbai);
        assertThat(dist).isBetween(1100.0, 1250.0);
    }

    @Test
    @DisplayName("Multi-stop optimization with 3 stops returns ordered route")
    void testMultiStopOptimization() {
        var origin = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var stops = List.of(
            new RouteOptimizationEngine.GeoPoint(28.7041, 77.1025),
            new RouteOptimizationEngine.GeoPoint(28.5355, 77.3910),
            new RouteOptimizationEngine.GeoPoint(28.6508, 77.2300)
        );

        var result = engine.optimizeMultiStop(origin, stops, null);

        assertThat(result.orderedStops()).hasSize(3);
        assertThat(result.totalDistanceKm()).isPositive();
        assertThat(result.estimatedDurationMinutes()).isPositive();
    }

    @Test
    @DisplayName("Multi-stop with empty list returns empty route")
    void testEmptyStops() {
        var origin = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var result = engine.optimizeMultiStop(origin, List.of(), null);
        assertThat(result.orderedStops()).isEmpty();
        assertThat(result.totalDistanceKm()).isZero();
    }

    @Test
    @DisplayName("A* path between two points returns valid path")
    void testAStarPath() {
        var start = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var goal = new RouteOptimizationEngine.GeoPoint(28.7041, 77.1025);
        var waypoints = List.of(
            new RouteOptimizationEngine.GeoPoint(28.6600, 77.1500)
        );

        var result = engine.aStarPath(start, goal, waypoints);

        assertThat(result.path()).isNotEmpty();
        assertThat(result.distanceKm()).isPositive();
        assertThat(result.durationMinutes()).isPositive();
    }

    @Test
    @DisplayName("Find nearest driver returns closest available driver")
    void testFindNearestDriver() {
        var pickup = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var drivers = List.of(
            new RouteOptimizationEngine.DriverLocation("driver-1", 28.6200, 77.2100), // Very close
            new RouteOptimizationEngine.DriverLocation("driver-2", 28.7500, 77.3000), // Far
            new RouteOptimizationEngine.DriverLocation("driver-3", 28.6150, 77.2150)  // Close
        );

        var assignment = engine.findNearestDriver(pickup, drivers);

        assertThat(assignment).isNotNull();
        assertThat(assignment.driverId()).isIn("driver-1", "driver-3"); // One of the closer ones
        assertThat(assignment.distanceKm()).isLessThan(5.0);
        assertThat(assignment.etaMinutes()).isPositive();
    }

    @Test
    @DisplayName("ETA estimation is positive and reasonable")
    void testEtaEstimation() {
        var from = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var to = new RouteOptimizationEngine.GeoPoint(28.6508, 77.2300); // ~5km

        long eta = engine.estimateEtaMinutes(from, to);
        assertThat(eta).isGreaterThan(0).isLessThan(60); // Should be < 1 hour for ~5km
    }

    @Test
    @DisplayName("Optimization with single stop returns that stop")
    void testSingleStop() {
        var origin = new RouteOptimizationEngine.GeoPoint(28.6139, 77.2090);
        var stop = new RouteOptimizationEngine.GeoPoint(28.7041, 77.1025);

        var result = engine.optimizeMultiStop(origin, List.of(stop), null);

        assertThat(result.orderedStops()).hasSize(1);
        assertThat(result.orderedStops().get(0)).isEqualTo(stop);
    }
}
