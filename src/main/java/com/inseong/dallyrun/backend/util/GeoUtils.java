package com.inseong.dallyrun.backend.util;

import java.util.List;

/**
 * 지구 표면 위 두 지점 간 거리 계산 유틸리티.
 * Haversine 공식을 사용하여 위도/경도 기반 대원 거리(great-circle distance)를 산출한다.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoUtils() {
    }

    /**
     * Haversine 공식으로 두 GPS 좌표 간 거리를 미터 단위로 계산한다.
     *
     * <p>Haversine 공식:
     * a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
     * c = 2 · atan2(√a, √(1−a))
     * d = R · c
     *
     * <p>지구를 완전한 구로 가정하므로 약 0.3% 이내의 오차가 발생할 수 있다.
     *
     * @return 두 지점 사이의 거리 (미터)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        // Haversine 중심각 계산: 위도·경도 차이로부터 구면 삼각법 적용
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * GPS 좌표 리스트의 인접 점 간 거리를 순차적으로 합산하여 총 이동 거리를 계산한다.
     *
     * @param points 각 원소가 {위도, 경도}인 좌표 배열 리스트
     * @return 총 이동 거리 (미터). 좌표가 2개 미만이면 0.0 반환
     */
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
