package org.itss.repository;

import org.itss.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findBySpotId(String spotId);
    Page<Review> findBySpotId(String spotId, Pageable pageable);
}
