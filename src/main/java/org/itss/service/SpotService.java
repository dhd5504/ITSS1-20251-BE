package org.itss.service;

import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.Result;

public interface SpotService {
    Result search(SpotSearchRequest req);
    Result getSpotById(String id);
}
