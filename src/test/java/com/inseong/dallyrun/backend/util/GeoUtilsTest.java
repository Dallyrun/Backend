package com.inseong.dallyrun.backend.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void calculateDistance_knownPoints() {
        // Seoul City Hall → Gangnam Station (~8.8km)
        double distance = GeoUtils.calculateDistance(
                37.5666, 126.9784,  // Seoul City Hall
                37.4979, 127.0276   // Gangnam Station
        );
        assertTrue(distance > 8000 && distance < 10000,
                "Expected ~8.8km but got " + distance);
    }

    @Test
    void calculateDistance_samePoint_returnsZero() {
        double distance = GeoUtils.calculateDistance(37.5666, 126.9784, 37.5666, 126.9784);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void calculateDistance_oppositePoints() {
        // North Pole to South Pole (~20,000km)
        double distance = GeoUtils.calculateDistance(90.0, 0.0, -90.0, 0.0);
        assertTrue(distance > 19_000_000 && distance < 21_000_000,
                "Expected ~20,000km but got " + distance / 1000 + "km");
    }

    @Test
    void calculateTotalDistance_multiplePoints() {
        List<double[]> points = List.of(
                new double[]{37.5666, 126.9784},
                new double[]{37.5700, 126.9800},
                new double[]{37.5750, 126.9850}
        );
        double total = GeoUtils.calculateTotalDistance(points);
        assertTrue(total > 0, "Total distance should be positive");
    }

    @Test
    void calculateTotalDistance_singlePoint_returnsZero() {
        List<double[]> points = List.of(new double[]{37.5666, 126.9784});
        assertEquals(0.0, GeoUtils.calculateTotalDistance(points));
    }

    @Test
    void calculateTotalDistance_emptyList_returnsZero() {
        assertEquals(0.0, GeoUtils.calculateTotalDistance(Collections.emptyList()));
    }

    @Test
    void calculateTotalDistance_null_returnsZero() {
        assertEquals(0.0, GeoUtils.calculateTotalDistance(null));
    }
}
