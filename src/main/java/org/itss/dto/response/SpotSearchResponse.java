package org.itss.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SpotSearchResponse {

    private List<SpotSummary> results;
    private long total;
    private int page;

    @Getter
    @Builder
    public static class SpotSummary {
        private String id;
        private String name;
        private String category;
        private String address;
        private double distance;
        private String hours;
        private String image;
        private double rating;
        private String price;
        private String phone;
        private String features;
        private List<org.itss.entity.Review> reviews;
    }
}
