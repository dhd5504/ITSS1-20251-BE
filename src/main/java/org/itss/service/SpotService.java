package org.itss.service;

import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.ApiResponse;

public interface SpotService {
    ApiResponse<?> search(SpotSearchRequest req);
    ApiResponse<?> getSpotById(String id);
}
