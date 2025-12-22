package org.itss.service.impl;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.review.ReviewCreateRequest;
import org.itss.dto.request.review.ReviewListRequest;
import org.itss.dto.request.review.ReviewUpdateRequest;
import org.itss.dto.request.spot.SpotSearchRequest;
import org.itss.dto.response.PaginationMeta;
import org.itss.dto.response.Result;
import org.itss.dto.response.review.ReviewResponse;
import org.itss.dto.response.spot.SpotDetailResponse;
import org.itss.dto.response.spot.SpotItemResponse;
import org.itss.entity.Review;
import org.itss.entity.Spot;
import org.itss.mapper.ReviewMapper;
import org.itss.mapper.SpotMapper;
import org.itss.repository.ReviewRepository;
import org.itss.repository.SpotRepository;
import org.itss.repository.UserRepository;
import org.itss.service.SpotService;
import org.itss.util.DistanceUtil;
import org.itss.util.RatingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SpotMapper spotMapper;
    private final ReviewMapper reviewMapper;

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

        List<Review> allReviews = reviewRepository.findBySpotId(id);
        double rating = RatingUtil.averageRating(allReviews);

        Pageable topFiveNewest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ReviewResponse> latestReviews = reviewRepository.findBySpotId(id, topFiveNewest)
                .getContent()
                .stream()
                .map(reviewMapper::toResponse)
                .toList();

        SpotDetailResponse detail = spotMapper.toDetail(spot, rating, allReviews.size(), latestReviews);

        return Result.ok(detail);
    }

    @Override
    public Result getSpotReviews(String id, ReviewListRequest req) {
        spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        int page = (req.getPage() == null || req.getPage() < 1) ? 1 : req.getPage();
        int limit = (req.getLimit() == null || req.getLimit() < 1) ? 20 : req.getLimit();
        String sortBy = req.getSortBy();

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<Review> reviewPage = reviewRepository.findBySpotId(id, pageable);
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(reviewMapper::toResponse)
                .toList();

        PaginationMeta meta = PaginationMeta.builder()
                .page(page)
                .total(reviewPage.getTotalElements())
                .build();

        return Result.ok(reviews, meta);
    }

    @Override
    public Result addReview(String spotId, ReviewCreateRequest req, String userId) {

        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        // Resolve user to get consistent userId (DB id)
        String userIdToSave = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setSpotId(spot.getId());
        review.setUserId(userIdToSave);
        review.setRating(req.getRating());
        review.setContent(req.getContent());

        Review saved = reviewRepository.save(review);

        return Result.ok(reviewMapper.toResponse(saved));
    }

    @Override
    public Result updateReview(String spotId, String reviewId, ReviewUpdateRequest req, String userId) {
        spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        String currentUserId = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getSpotId().equals(spotId)) {
            throw new RuntimeException("Review does not belong to this spot");
        }

        if (!review.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: cannot update others' review");
        }

        if (req.getRating() != null) {
            review.setRating(req.getRating());
        }
        if (req.getContent() != null && !req.getContent().isBlank()) {
            review.setContent(req.getContent());
        }

        Review saved = reviewRepository.save(review);
        return Result.ok(reviewMapper.toResponse(saved));
    }

    @Override
    public Result deleteReview(String spotId, String reviewId, String userId) {
        spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        String currentUserId = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getSpotId().equals(spotId)) {
            throw new RuntimeException("Review does not belong to this spot");
        }

        if (!review.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: cannot delete others' review");
        }

        reviewRepository.delete(review);
        return Result.ok("Deleted");
    }

    private Sort resolveSort(String sortBy) {
        if ("oldest".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        }
        if ("rating_high".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
