package org.itss.controller;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.ApiResponse;
import org.itss.service.SpotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @GetMapping("/search")
    public ApiResponse<?> search(SpotSearchRequest req) {
        return spotService.search(req);
    }
}
