package org.itss.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpotSearchRequest {
    private String q;  // từ khóa tìm kiếm theo tên quán
    private String category;  //loại : coffe, tea,..
    private String group;
    private String suitable;
    private String price;
    private Double lat;  // vĩ độ của user
    private Double lng;  // kinh độ của user
    private Double maxDistance;  // Khoảng cách tối đa
    private Double minRating;  // rate tối thiểu
    private String sortBy;  // Lọc theo: rating , distance , popular
    private Integer limit = 10;
    private Integer page = 1;
}
