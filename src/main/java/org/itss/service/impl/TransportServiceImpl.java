package org.itss.service.impl;

import org.itss.service.TransportService;
import org.springframework.stereotype.Service;

@Service
public class TransportServiceImpl implements TransportService {

    @Override
    public String recommendTransport(double distanceKm, String weather, String traffic) {
        if (distanceKm < 1.0) {
            return "WALK";
        } else if (distanceKm < 3.0) {
            return "BICYCLE";
        } else if (distanceKm < 5.0) {
            return "MOTORBIKE";
        } else {
            return "CAR/TAXI";
        }
    }
}
