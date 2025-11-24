package org.itss.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationMeta {
    private int page;
    private long total;
}
