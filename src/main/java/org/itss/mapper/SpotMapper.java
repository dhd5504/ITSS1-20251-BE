package org.itss.mapper;

import org.itss.dto.response.SpotSearchResponse;
import org.itss.entity.Spot;
import org.springframework.stereotype.Component;

@Component
public class SpotMapper {

    public SpotSearchResponse.SpotSummary toSummary(
            Spot spot,
            double rating,
            double distance
    ) {
        return SpotSearchResponse.SpotSummary.builder()
                .id(spot.getId())
                .name(spot.getName())
                .location(spot.getLocation())
                .imageUrl(spot.getImageUrl())
                .category(spot.getCategory())
                .rating(rating)
                .distance(distance)
                .build();
    }
}
