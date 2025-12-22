package org.itss.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class ExternalSimService {
    private final Random random = new Random();

    public String getCurrentWeather() {
        String[] weather = {"SUNNY", "CLOUDY", "RAINY", "STORM"};
        return weather[random.nextInt(weather.length)];
    }

    public String getCurrentTraffic() {
        String[] traffic = {"LOW", "NORMAL", "HEAVY", "JAM"};
        return traffic[random.nextInt(traffic.length)];
    }
}
