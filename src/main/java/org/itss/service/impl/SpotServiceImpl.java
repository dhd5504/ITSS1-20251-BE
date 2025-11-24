package org.itss.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResponse<?> search(SpotSearchRequest req) {

        // 1) Filter cơ bản theo q + category + group + suitable + price
        List<Spot> filtered = spotRepository.findAll().stream()
                .filter(s -> req.getQ() == null ||
                        s.getName().toLowerCase().contains(req.getQ().toLowerCase()))
                .filter(s -> req.getCategory() == null ||
                        s.getCategory().equalsIgnoreCase(req.getCategory()))
                .filter(s -> matchesFeature(s, "group", req.getGroup()))
                .filter(s -> matchesFeature(s, "suitable", req.getSuitable()))
                .filter(s -> matchesPrice(s, req.getPrice()))
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

                    return spotMapper.toSummary(spot, rating, distance, reviews);
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

    @Override
    public ApiResponse<?> getSpotById(String id) {
        // Tìm spot theo ID
        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found with id: " + id));

        // Lấy danh sách reviews và tính rating
        List<Review> reviews = reviewRepository.findBySpotId(spot.getId());
        double rating = RatingUtil.averageRating(reviews);

         // Map sang DTO (distance = 0 vì không có vị trí user)
        SpotSearchResponse.SpotSummary spotDetail = spotMapper.toSummary(spot, rating, 0, reviews);

        // Trả về response
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(spotDetail)
                .build();
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
