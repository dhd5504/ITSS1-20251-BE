package org.itss.util;

public class DistanceUtil {

    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // km

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        
        // Clamp a to [0, 1] to avoid NaN due to floating point errors
        a = Math.min(1.0, Math.max(0.0, a));

        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
