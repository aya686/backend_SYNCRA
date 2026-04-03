package org.example.backend_pi.service;

import org.example.backend_pi.dto.ReviewDTO;
import org.example.backend_pi.entity.Review;
import java.util.List;

public interface ReviewService {

    Review addMachineReview(ReviewDTO reviewDTO);

    Review addServiceReview(ReviewDTO reviewDTO);

    Review updateReview(Long id, ReviewDTO reviewDTO);

    void deleteReview(Long id);

    List<Review> getReviewsByMachine(Long machineId);

    List<Review> getReviewsByService(Long serviceId);

    List<Review> getReviewsByUser(Long userId);

    Double getAverageRatingForMachine(Long machineId);

    Double getAverageRatingForService(Long serviceId);
}
