package org.itss.mapper;

import org.itss.dto.response.SpotSearchResponse;
import org.itss.entity.Spot;
import org.springframework.stereotype.Component;

@Component
public class SpotMapper {

    public SpotSearchResponse.SpotSummary toSummary(
            Spot spot,
            double rating,
            double distance,
            java.util.List<org.itss.entity.Review> reviews
    ) {
        return SpotSearchResponse.SpotSummary.builder()
                .id(spot.getId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getLocation())
                .distance(distance)
                .hours(spot.isAlwaysOpen() ? "24時間営業" : spot.getOpenTime() + "-" + spot.getCloseTime())
                .image(spot.getImageUrl())
                .rating(rating)
                .price(spot.getPricing())
                .phone(spot.getPhone())
                .features(spot.getFeatures())
                .reviews(reviews)
                .build();
    }
}
