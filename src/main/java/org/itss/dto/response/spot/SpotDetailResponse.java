package org.itss.dto.response.spot;

import lombok.Builder;
import lombok.Getter;
import org.itss.dto.response.review.ReviewResponse;

import java.util.List;

@Getter
@Builder
public class SpotDetailResponse {
    private String id;
    private String name;
    private String category;
    private String address;
    private String hours;
    private String image;
    private double rating;
    private int reviewCount;
    private String price;
    private String phone;
    private String features;
    private String description;
    private Double lat;
    private Double lng;
    private List<ReviewResponse> reviews;
}
