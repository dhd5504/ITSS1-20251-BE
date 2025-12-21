package org.itss.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.itss.dto.request.review.ReviewCreateRequest;
import org.itss.dto.request.review.ReviewUpdateRequest;
import org.itss.dto.request.review.ReviewListRequest;
import org.itss.dto.request.spot.SpotSearchRequest;
import org.itss.dto.response.Result;
import org.itss.service.SpotService;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/{id}/reviews")
    public Result getSpotReviews(
            @PathVariable String id,
            @RequestParam(name = "sort_by", required = false) String sortBySnake,
            @RequestParam(name = "sortBy", required = false) String sortByCamel,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "page", required = false) Integer page
    ) {
        ReviewListRequest req = new ReviewListRequest();
        req.setSortBy(sortBySnake != null ? sortBySnake : sortByCamel);
        req.setLimit(limit);
        req.setPage(page);
        return spotService.getSpotReviews(id, req);
    }

    @GetMapping("/search")
    public Result search(SpotSearchRequest req) {
        return spotService.search(req);
    }

    @PostMapping("/{id}/reviews")
    public Result createReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewCreateRequest req,
            Authentication authentication
    ) {
        String userId = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.addReview(id, req, userId);
    }

    @PutMapping("/{spotId}/reviews/{reviewId}")
    public Result updateReview(
            @PathVariable String spotId,
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewUpdateRequest req,
            Authentication authentication
    ) {
        String userId = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.updateReview(spotId, reviewId, req, userId);
    }

    @DeleteMapping("/{spotId}/reviews/{reviewId}")
    public Result deleteReview(
            @PathVariable String spotId,
            @PathVariable String reviewId,
            Authentication authentication
    ) {
        String userId = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.deleteReview(spotId, reviewId, userId);
    }
}
