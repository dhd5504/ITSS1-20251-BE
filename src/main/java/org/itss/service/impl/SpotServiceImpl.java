package org.itss.service.impl;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.SpotSearchRequest;
import org.itss.dto.response.PaginationMeta;
import org.itss.dto.response.Result;
import org.itss.dto.response.spot.SpotItemResponse;
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
    public Result search(SpotSearchRequest req) {

        // 1) Filter theo tên + category
        List<Spot> filtered = spotRepository.findAll().stream()
                .filter(s -> req.getQ() == null ||
                        s.getName().toLowerCase().contains(req.getQ().toLowerCase()))
                .filter(s -> req.getCategory() == null ||
                        s.getCategory().equalsIgnoreCase(req.getCategory()))
                .collect(Collectors.toList());

        // 2) Map sang DTO item
        List<SpotItemResponse> mapped = filtered.stream()
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

                    return spotMapper.toItem(spot, rating, distance, reviews);
                })
                .collect(Collectors.toList());

        // 3) Filter theo rating/distance
        mapped = mapped.stream()
                .filter(s -> req.getMinRating() == null || s.getRating() >= req.getMinRating())
                .filter(s -> req.getMaxDistance() == null || s.getDistance() <= req.getMaxDistance())
                .collect(Collectors.toList());

        // 4) Sort
        if ("rating".equalsIgnoreCase(req.getSortBy())) {
            mapped.sort(Comparator.comparingDouble(SpotItemResponse::getRating).reversed());
        } else if ("distance".equalsIgnoreCase(req.getSortBy())) {
            mapped.sort(Comparator.comparingDouble(SpotItemResponse::getDistance));
        }

        // 5) Pagination
        int total = mapped.size();
        int start = (req.getPage() - 1) * req.getLimit();
        int end = Math.min(start + req.getLimit(), total);

        List<SpotItemResponse> paged =
                (start < end) ? mapped.subList(start, end) : List.of();

        // Meta
        PaginationMeta meta = PaginationMeta.builder()
                .page(req.getPage())
                .total(total)
                .build();

        return Result.ok(paged, meta);
    }

    @Override
    public Result getSpotById(String id) {

        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        List<Review> reviews = reviewRepository.findBySpotId(id);
        double rating = RatingUtil.averageRating(reviews);

        SpotItemResponse item = spotMapper.toItem(spot, rating, 0, reviews);

        return Result.ok(item);
    }
}
