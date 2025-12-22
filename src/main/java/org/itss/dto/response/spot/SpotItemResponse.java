package org.itss.dto.response.spot;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SpotItemResponse {
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
    private String recommendedTransport;
    private List<?> reviews;   // có thể đổi sang DTO review riêng nếu muốn
}
