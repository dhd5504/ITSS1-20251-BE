package org.itss.mapper;

import org.itss.dto.response.review.ReviewResponse;
import org.itss.dto.response.spot.SpotDetailResponse;
import org.itss.dto.response.spot.SpotItemResponse;
import org.itss.entity.Review;
import org.itss.entity.Spot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpotMapper {

    public SpotItemResponse toItem(
            Spot spot,
            double rating,
            double distance,
            List<Review> reviews,
            String recommendedTransport
    ) {
        return SpotItemResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getLocation())
                .distance(distance)
                .hours(resolveWorkingHours(spot))
                .image(spot.getImageUrl())
                .rating(rating)
                .price(spot.getPricing())
                .phone(spot.getPhone())
                .features(spot.getFeatures())
                .recommendedTransport(recommendedTransport)
                .reviews(reviews)
                .build();
    }

    public SpotDetailResponse toDetail(
            Spot spot,
            double rating,
            int reviewCount,
            List<ReviewResponse> reviews
    ) {
        return SpotDetailResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getLocation())
                .hours(resolveWorkingHours(spot))
                .image(spot.getImageUrl())
                .rating(rating)
                .reviewCount(reviewCount)
                .price(spot.getPricing())
                .phone(spot.getPhone())
                .features(spot.getFeatures())
                .description(spot.getLocation())
                .lat(spot.getLat())
                .lng(spot.getLng())
                .reviews(reviews)
                .build();
    }

    private String resolveWorkingHours(Spot spot) {
        if (spot.isAlwaysOpen()) {
            return "24/7"; // always open
        }
        String open = safe(spot.getOpenTime());
        String close = safe(spot.getCloseTime());
        return open + " - " + close;
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}
