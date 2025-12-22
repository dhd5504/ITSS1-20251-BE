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
import org.springframework.lang.NonNull;
import java.util.Objects;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @GetMapping("/favorites")
    public Result getFavorites(Authentication authentication) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.getFavorites(Objects.requireNonNull(name));
    }

    @GetMapping("/search")
    public Result search(@NonNull SpotSearchRequest req) {
        return spotService.search(Objects.requireNonNull(req));
    }

    @GetMapping("/{id}")
    public Result getSpotById(@PathVariable @NonNull String id) {
        return spotService.getSpotById(Objects.requireNonNull(id));
    }

    @GetMapping("/{id}/reviews")
    public Result getSpotReviews(
            @PathVariable @NonNull String id,
            @RequestParam(name = "sort_by", required = false) String sortBySnake,
            @RequestParam(name = "sortBy", required = false) String sortByCamel,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "page", required = false) Integer page
    ) {
        ReviewListRequest req = new ReviewListRequest();
        req.setSortBy(sortBySnake != null ? sortBySnake : sortByCamel);
        req.setLimit(limit);
        req.setPage(page);
        return spotService.getSpotReviews(Objects.requireNonNull(id), Objects.requireNonNull(req));
    }

    @PostMapping("/{id}/reviews")
    public Result createReview(
            @PathVariable @NonNull String id,
            @Valid @RequestBody @NonNull ReviewCreateRequest req,
            Authentication authentication
    ) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.addReview(id, req, Objects.requireNonNull(name));
    }

    @PutMapping("/{spotId}/reviews/{reviewId}")
    public Result updateReview(
            @PathVariable @NonNull String spotId,
            @PathVariable @NonNull String reviewId,
            @Valid @RequestBody @NonNull ReviewUpdateRequest req,
            Authentication authentication
    ) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.updateReview(spotId, reviewId, req, Objects.requireNonNull(name));
    }

    @DeleteMapping("/{spotId}/reviews/{reviewId}")
    public Result deleteReview(
            @PathVariable @NonNull String spotId,
            @PathVariable @NonNull String reviewId,
            Authentication authentication
    ) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.deleteReview(spotId, reviewId, Objects.requireNonNull(name));
    }

    @PostMapping("/{id}/favorite")
    public Result addFavorite(@PathVariable @NonNull String id, Authentication authentication) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.addFavorite(id, Objects.requireNonNull(name));
    }

    @DeleteMapping("/{id}/favorite")
    public Result removeFavorite(@PathVariable @NonNull String id, Authentication authentication) {
        String name = (authentication != null) ? authentication.getName() : "anonymous";
        return spotService.removeFavorite(id, Objects.requireNonNull(name));
    }

}
