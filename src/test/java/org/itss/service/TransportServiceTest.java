package org.itss.service;

import org.itss.service.impl.TransportServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransportServiceTest {

    private final TransportService transportService = new TransportServiceImpl();

    @Test
    public void testWalk() {
        assertEquals("WALK", transportService.recommendTransport(0.5, "SUNNY", "LOW"));
    }

    @Test
    public void testBicycle() {
        assertEquals("BICYCLE", transportService.recommendTransport(2.0, "SUNNY", "LOW"));
    }

    @Test
    public void testMotorbike() {
        assertEquals("MOTORBIKE", transportService.recommendTransport(4.0, "SUNNY", "LOW"));
    }

    @Test
    public void testCar() {
        assertEquals("CAR/TAXI", transportService.recommendTransport(6.0, "SUNNY", "LOW"));
    }
}
