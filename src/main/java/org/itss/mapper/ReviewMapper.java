package org.itss.mapper;

import lombok.RequiredArgsConstructor;
import org.itss.dto.response.review.ReviewResponse;
import org.itss.entity.Review;
import org.itss.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final UserRepository userRepository;

    public ReviewResponse toResponse(Review review) {
        String userName = review.getUserId();
        try {
            Long uid = Long.parseLong(review.getUserId());
            userName = userRepository.findById(uid)
                    .map(u -> u.getUsername())
                    .orElse(userName);
        } catch (Exception ignored) {
            // fallback to existing userId if not numeric
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(userName)
                .spotId(review.getSpotId())
                .rating(review.getRating())
                .content(review.getContent())
                .comment(review.getContent())
                .createdAt(review.getCreatedAt())
                .date(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .tags(Collections.emptyList())
                .build();
    }
}
