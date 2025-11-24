package org.itss.controller;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.Result;
import org.itss.service.SpotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @GetMapping("/{id}")
    public Result getSpotById(@PathVariable String id) {
        return spotService.getSpotById(id);
    }

    @GetMapping("/search")
    public Result search(SpotSearchRequest req) {
        return spotService.search(req);
    }
}
