package com.inseong.dallyrun.backend.util;

import java.util.List;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoUtils() {
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    public static double calculateTotalDistance(List<double[]> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 1; i < points.size(); i++) {
            total += calculateDistance(
                    points.get(i - 1)[0], points.get(i - 1)[1],
                    points.get(i)[0], points.get(i)[1]);
        }
        return total;
    }
}
