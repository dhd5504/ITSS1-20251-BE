package org.itss.util;

import org.itss.entity.Review;

import java.util.List;

public class RatingUtil {

    public static double averageRating(List<Review> reviews) {
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
    }
}
