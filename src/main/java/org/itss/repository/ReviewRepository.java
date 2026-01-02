package org.itss.repository;

import org.springframework.lang.NonNull;

import org.itss.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findBySpotId(@NonNull String spotId);
    Page<Review> findBySpotId(@NonNull String spotId, @NonNull Pageable pageable);

    boolean existsBySpotIdAndUserId(@NonNull String spotId, @NonNull String userId);
}
