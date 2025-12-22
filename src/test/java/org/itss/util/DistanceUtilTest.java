package org.itss.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceUtilTest {

    @Test
    public void testHaversineSamePoint() {
        double lat = 21.0285;
        double lng = 105.8542;
        double distance = DistanceUtil.haversine(lat, lng, lat, lng);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    public void testHaversineHanoiToHCM() {
        // Hanoi: 21.0285, 105.8542
        // Ho Chi Minh City: 10.7626, 106.6602
        double distance = DistanceUtil.haversine(21.0285, 105.8542, 10.7626, 106.6602);
        // Expected distance is ~1137 km
        assertEquals(1137.0, distance, 10.0);
    }

    @Test
    public void testHaversineAntipodal() {
        // Antipodal points: (0, 0) and (0, 180) - distance should be half circumference ~20015 km
        double distance = DistanceUtil.haversine(0.0, 0.0, 0.0, 180.0);
        assertTrue(!Double.isNaN(distance));
        assertEquals(Math.PI * 6371.0, distance, 1.0);
    }

    @Test
    public void testHaversineClamping() {
        // Test case that might cause a > 1 due to precision
        // Using points that are very far apart
        double distance = DistanceUtil.haversine(90.0, 0.0, -90.0, 0.0);
        assertTrue(!Double.isNaN(distance));
        assertEquals(Math.PI * 6371.0, distance, 1.0);
    }
}
