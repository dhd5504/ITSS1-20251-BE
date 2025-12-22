package org.itss.service;

import org.springframework.lang.NonNull;

import org.itss.dto.request.review.ReviewCreateRequest;
import org.itss.dto.request.review.ReviewListRequest;
import org.itss.dto.request.review.ReviewUpdateRequest;
import org.itss.dto.request.spot.SpotSearchRequest;
import org.itss.dto.response.Result;

public interface SpotService {
    Result search(@NonNull SpotSearchRequest req);
    Result getSpotById(@NonNull String id);
    Result getSpotReviews(@NonNull String id, @NonNull ReviewListRequest req);
    Result addReview(@NonNull String spotId, @NonNull ReviewCreateRequest req, @NonNull String userId);
    Result updateReview(@NonNull String spotId, @NonNull String reviewId, @NonNull ReviewUpdateRequest req, @NonNull String userId);
    Result deleteReview(@NonNull String spotId, @NonNull String reviewId, @NonNull String userId);

    Result addFavorite(@NonNull String spotId, @NonNull String userId);

    Result removeFavorite(@NonNull String spotId, @NonNull String userId);

    Result getFavorites(@NonNull String userId);
}
