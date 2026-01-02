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
import org.itss.entity.User;
import org.itss.mapper.ReviewMapper;
import org.itss.mapper.SpotMapper;
import org.itss.repository.ReviewRepository;
import org.itss.repository.SpotRepository;
import org.itss.repository.UserRepository;
import org.itss.service.ExternalSimService;
import org.itss.service.SpotService;
import org.itss.service.TransportService;
import org.itss.util.DistanceUtil;
import org.itss.util.RatingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SpotMapper spotMapper;
    private final ReviewMapper reviewMapper;
    private final TransportService transportService;
    private final ExternalSimService externalSimService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result search(@NonNull SpotSearchRequest req) {

        // 1) Filter theo tên + category
        List<Spot> filtered = spotRepository.findAll().stream()
                .filter(s -> req.getQ() == null ||
                        s.getName().toLowerCase().contains(req.getQ().toLowerCase()))
                .filter(s -> req.getCategory() == null ||
                        s.getCategory().equalsIgnoreCase(req.getCategory()))
                .filter(s -> matchesFeature(s, "group", req.getGroup()))
                .filter(s -> matchesFeature(s, "suitable", req.getSuitable()))
                .filter(s -> matchesPrice(s, req.getPrice()))
                .collect(Collectors.toList());

        // 2) Map sang DTO item
        List<SpotItemResponse> mapped = filtered.stream()
                .map(spot -> {

                    List<Review> reviews = reviewRepository.findBySpotId(Objects.requireNonNull(spot.getId()));
                    double rating = RatingUtil.averageRating(Objects.requireNonNull(reviews));

                    double distance = 0;
                    if (req.getLat() != null && req.getLng() != null) {
                        distance = DistanceUtil.haversine(
                                req.getLat(), req.getLng(),
                                Objects.requireNonNull(spot.getLat()), Objects.requireNonNull(spot.getLng())
                        );
                    }

                    String weather = externalSimService.getCurrentWeather();
                    String traffic = externalSimService.getCurrentTraffic();
                    String recommendedTransport = transportService.recommendTransport(distance, weather, traffic);

                    return spotMapper.toItem(spot, rating, distance, reviews, recommendedTransport);
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
    public Result getSpotById(@NonNull String id) {

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
    public Result getSpotReviews(@NonNull String id, @NonNull ReviewListRequest req) {
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
    public Result addReview(@NonNull String spotId, @NonNull ReviewCreateRequest req, @NonNull String userId) {

        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        // Resolve user to get consistent userId (DB id)
        String userIdToSave = userRepository.findByUsername(Objects.requireNonNull(userId))
                .or(() -> userRepository.findByEmail(Objects.requireNonNull(userId)))
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Each user can leave only one review per spot
        if (reviewRepository.existsBySpotIdAndUserId(spotId, userIdToSave)) {
            return Result.error("You have already reviewed this spot");
        }

        Review review = new Review();
        review.setSpotId(spot.getId());
        review.setUserId(userIdToSave);
        review.setRating(req.getRating());
        review.setContent(req.getContent());

        Review saved = reviewRepository.save(review);

        return Result.ok(reviewMapper.toResponse(saved));
    }

    @Override
    public Result updateReview(@NonNull String spotId, @NonNull String reviewId, @NonNull ReviewUpdateRequest req, @NonNull String userId) {
        spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        String currentUserId = userRepository.findByUsername(Objects.requireNonNull(userId))
                .or(() -> userRepository.findByEmail(Objects.requireNonNull(userId)))
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
    public Result deleteReview(@NonNull String spotId, @NonNull String reviewId, @NonNull String userId) {
        spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        String currentUserId = userRepository.findByUsername(Objects.requireNonNull(userId))
                .or(() -> userRepository.findByEmail(Objects.requireNonNull(userId)))
                .map(u -> String.valueOf(u.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getSpotId().equals(spotId)) {
            throw new RuntimeException("Review does not belong to this spot");
        }

        if (!review.getUserId().equals(currentUserId)) {
            return Result.error("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
        return Result.ok("Review deleted");
    }

    @Override
    @Transactional
    public Result addFavorite(@NonNull String spotId, @NonNull String userId) {
        User user = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        user.getFavorites().add(spot);
        userRepository.save(user);
        return Result.ok("Spot added to favorites");
    }

    @Override
    @Transactional
    public Result removeFavorite(@NonNull String spotId, @NonNull String userId) {
        User user = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        user.getFavorites().remove(spot);
        userRepository.save(user);
        return Result.ok("Spot removed from favorites");
    }

    @Override
    public Result getFavorites(@NonNull String userId) {
        User user = userRepository.findByUsername(userId)
                .or(() -> userRepository.findByEmail(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SpotItemResponse> favorites = user.getFavorites().stream()
                .map(spot -> {
                    List<Review> reviews = reviewRepository.findBySpotId(Objects.requireNonNull(spot.getId()));
                    double rating = RatingUtil.averageRating(Objects.requireNonNull(reviews));
                    String weather = externalSimService.getCurrentWeather();
                    String traffic = externalSimService.getCurrentTraffic();
                    String recommendedTransport = transportService.recommendTransport(0.0, weather, traffic);
                    return spotMapper.toItem(spot, rating, 0.0, reviews, recommendedTransport);
                })
                .collect(Collectors.toList());

        return Result.ok(favorites);
    }

    @NonNull
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

    /**
     * Helper method để kiểm tra xem spot có match với feature filter không
     * @param spot Spot cần kiểm tra
     * @param fieldName Tên field trong JSON (vd: "group", "suitable")
     * @param filterValue Giá trị cần filter (vd: "big", "all")
     * @return true nếu match hoặc filterValue == null
     */
    private boolean matchesFeature(Spot spot, String fieldName, String filterValue) {
        // Nếu không có filter value thì pass
        if (filterValue == null || filterValue.isEmpty()) {
            return true;
        }

        // Nếu spot không có features thì không match
        if (spot.getFeatures() == null || spot.getFeatures().isEmpty()) {
            return false;
        }

        try {
            // Parse JSON từ features string
            JsonNode featuresJson = objectMapper.readTree(spot.getFeatures());
            
            // Lấy giá trị của field cần kiểm tra
            JsonNode fieldNode = featuresJson.get(fieldName);
            
            if (fieldNode == null) {
                return false;
            }
            
            // So sánh giá trị (case-insensitive)
            String fieldValue = fieldNode.asText();
            return fieldValue.equalsIgnoreCase(filterValue);
            
        } catch (Exception e) {
            // Nếu parse lỗi thì không match
            return false;
        }
    }

    /**
     * Helper method để kiểm tra xem spot có match với price filter không
     * @param spot Spot cần kiểm tra
     * @param priceFilter Giá trị filter: "free" hoặc "<=150000"
     * @return true nếu match hoặc priceFilter == null
     */
    private boolean matchesPrice(Spot spot, String priceFilter) {
        // Nếu không có filter thì pass
        if (priceFilter == null || priceFilter.isEmpty()) {
            return true;
        }

        // Nếu spot không có pricing thì không match
        if (spot.getPricing() == null || spot.getPricing().isEmpty()) {
            return false;
        }

        try {
            // Parse JSON từ pricing string
            JsonNode pricingJson = objectMapper.readTree(spot.getPricing());

            // Case 1: Filter "free" - chỉ lấy những spot miễn phí
            if ("free".equalsIgnoreCase(priceFilter)) {
                JsonNode isFreeNode = pricingJson.get("isFree");
                return isFreeNode != null && isFreeNode.asBoolean();
            }

            // Case 2: Filter "<=150000" - lấy những spot có giá <= 150,000đ
            if (priceFilter.startsWith("<=")) {
                // Lấy ngưỡng giá (150000)
                int maxPrice = Integer.parseInt(priceFilter.substring(2).trim());

                // Kiểm tra isFree trước - nếu free thì luôn pass
                JsonNode isFreeNode = pricingJson.get("isFree");
                if (isFreeNode != null && isFreeNode.asBoolean()) {
                    return true;
                }

                // Lấy giá cao nhất từ các trường khác nhau
                Integer highestPrice = extractHighestPrice(pricingJson);
                
                // Nếu không parse được giá thì không match
                if (highestPrice == null) {
                    return false;
                }

                return highestPrice <= maxPrice;
            }

            return false;

        } catch (Exception e) {
            // Nếu parse lỗi thì không match
            return false;
        }
    }

    /**
     * Extract giá cao nhất từ pricing JSON
     * Xử lý các trường: min/max, adult, student, children, ticket, card_fee
     */
    private Integer extractHighestPrice(JsonNode pricingJson) {
        Integer highestPrice = null;

        // Kiểm tra các trường có thể chứa giá
        String[] priceFields = {"max", "min", "adult", "student", "children", "ticket", "card_fee"};

        for (String field : priceFields) {
            JsonNode fieldNode = pricingJson.get(field);
            if (fieldNode != null && !fieldNode.isNull()) {
                Integer price = parsePrice(fieldNode.asText());
                if (price != null) {
                    if (highestPrice == null || price > highestPrice) {
                        highestPrice = price;
                    }
                }
            }
        }

        return highestPrice;
    }

    /**
     * Parse price string thành integer
     * Xóa các ký tự không phải số (đ, ., ,, /year, etc.)
     */
    private Integer parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return null;
        }

        try {
            // Xóa các ký tự không phải số: đ, ., ,, khoảng trắng, /year, etc.
            String cleanPrice = priceStr
                    .replaceAll("[đ,./\\s]", "")
                    .replaceAll("/year", "")
                    .trim();

            if (cleanPrice.isEmpty()) {
                return null;
            }

            return Integer.parseInt(cleanPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
