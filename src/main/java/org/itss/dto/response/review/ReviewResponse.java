package org.itss.dto.response.review;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private String id;
    private String userId;
    private String userName;
    private String spotId;
    private Integer rating;
    private String content;
    private String comment;
    private LocalDateTime createdAt;
    private String date;
    private List<String> tags;
}
