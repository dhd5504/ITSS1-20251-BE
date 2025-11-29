package org.itss.mapper;

import org.itss.dto.response.spot.SpotItemResponse;
import org.itss.entity.Spot;
import org.itss.entity.Review;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpotMapper {

    public SpotItemResponse toItem(
            Spot spot,
            double rating,
            double distance,
            List<Review> reviews
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
                .reviews(reviews)
                .build();
    }

    private String resolveWorkingHours(Spot spot) {
        if (spot.isAlwaysOpen()) {
            return "24時間営業"; // tiếng Nhật: 24-hour open
        }
        String open = safe(spot.getOpenTime());
        String close = safe(spot.getCloseTime());
        return open + " - " + close;
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}
