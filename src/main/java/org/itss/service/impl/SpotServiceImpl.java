package org.itss.service.impl;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.ApiResponse;
import org.itss.dto.response.PaginationMeta;
import org.itss.dto.response.SpotSearchResponse;
import org.itss.entity.Review;
import org.itss.entity.Spot;
import org.itss.mapper.SpotMapper;
import org.itss.repository.ReviewRepository;
import org.itss.repository.SpotRepository;
import org.itss.service.SpotService;
import org.itss.util.DistanceUtil;
import org.itss.util.RatingUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;
    private final ReviewRepository reviewRepository;
    private final SpotMapper spotMapper;

    @Override
    public ApiResponse<?> search(SpotSearchRequest req) {

        // 1) Filter cơ bản theo q + category
        List<Spot> filtered = spotRepository.findAll().stream()
                .filter(s -> req.getQ() == null ||
                        s.getName().toLowerCase().contains(req.getQ().toLowerCase()))
                .filter(s -> req.getCategory() == null ||
                        s.getCategory().equalsIgnoreCase(req.getCategory()))
                .collect(Collectors.toList());

        // 2) Map sang DTO + tính rating + distance
        List<SpotSearchResponse.SpotSummary> mapped = filtered.stream()
                .map(spot -> {

                    List<Review> reviews = reviewRepository.findBySpotId(spot.getId());
                    double rating = RatingUtil.averageRating(reviews);

                    double distance = 0;
                    if (req.getLat() != null && req.getLng() != null) {
                        distance = DistanceUtil.haversine(
                                req.getLat(), req.getLng(),
                                spot.getLat(), spot.getLng()
                        );
                    }

                    return spotMapper.toSummary(spot, rating, distance);
                })
                .collect(Collectors.toList());

        // 3) Filter thêm theo rating & distance
        mapped = mapped.stream()
                .filter(s -> req.getMinRating() == null || s.getRating() >= req.getMinRating())
                .filter(s -> req.getMaxDistance() == null || s.getDistance() <= req.getMaxDistance())
                .collect(Collectors.toList());

        // 4) Sort theo rating / distance
        if ("rating".equalsIgnoreCase(req.getSortBy())) {
            mapped.sort(Comparator.comparingDouble(SpotSearchResponse.SpotSummary::getRating).reversed());
        } else if ("distance".equalsIgnoreCase(req.getSortBy())) {
            mapped.sort(Comparator.comparingDouble(SpotSearchResponse.SpotSummary::getDistance));
        }

        // 5) Pagination
        int total = mapped.size();
        int start = (req.getPage() - 1) * req.getLimit();
        int end = Math.min(start + req.getLimit(), total);

        List<SpotSearchResponse.SpotSummary> paged =
                (start < end) ? mapped.subList(start, end) : List.of();

        // 6) Response Wrapper
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(paged)   // ⬅ trả trực tiếp list kết quả
                .meta(
                        PaginationMeta.builder()
                                .page(req.getPage())
                                .total(total)
                                .build()
                )
                .build();
    }
}
