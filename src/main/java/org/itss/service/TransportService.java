package org.itss.service;

public interface TransportService {
    String recommendTransport(double distanceKm, String weather, String traffic);
}
