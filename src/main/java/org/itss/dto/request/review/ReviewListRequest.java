package org.itss.dto.request.review;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewListRequest {
    private String sortBy;
    private Integer limit = 20;
    private Integer page = 1;
}
