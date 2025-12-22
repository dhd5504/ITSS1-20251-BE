package org.itss.service;

import org.itss.dto.request.review.ReviewCreateRequest;
import org.itss.dto.request.review.ReviewListRequest;
import org.itss.dto.request.review.ReviewUpdateRequest;
import org.itss.dto.request.spot.SpotSearchRequest;
import org.itss.dto.response.Result;

public interface SpotService {
    Result search(SpotSearchRequest req);
    Result getSpotById(String id);
    Result getSpotReviews(String id, ReviewListRequest req);
    Result addReview(String spotId, ReviewCreateRequest req, String userId);
    Result updateReview(String spotId, String reviewId, ReviewUpdateRequest req, String userId);
    Result deleteReview(String spotId, String reviewId, String userId);
}
