package org.example.backend_pi.service;

// service/impl/ReviewServiceImpl.java

import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.entity.Review;
import org.example.backend_pi.repository.MachineRepository;
import org.example.backend_pi.repository.ServiceRepository;
import org.example.backend_pi.repository.ReviewRepository;
import org.example.backend_pi.service.ReviewService;
import org.example.backend_pi.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public Review addMachineReview(ReviewDTO reviewDTO) {
        // Vérifier si la machine existe
        Machine machine = machineRepository.findById(reviewDTO.getMachineId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée avec l'ID: " + reviewDTO.getMachineId()));

        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setUserId(reviewDTO.getUserId());
        review.setUserName(reviewDTO.getUserName());
        review.setUserAvatarUrl(reviewDTO.getUserAvatarUrl());
        review.setMachine(machine);

        Review savedReview = reviewRepository.save(review);

        // Mettre à jour la note moyenne de la machine
        updateMachineAverageRating(machine.getId());

        return savedReview;
    }

    @Override
    public Review addServiceReview(ReviewDTO reviewDTO) {
        // Vérifier si le service existe
        ServiceEntity service = serviceRepository.findById(reviewDTO.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + reviewDTO.getServiceId()));

        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setUserId(reviewDTO.getUserId());
        review.setUserName(reviewDTO.getUserName());
        review.setUserAvatarUrl(reviewDTO.getUserAvatarUrl());
        review.setService(service);

        Review savedReview = reviewRepository.save(review);

        // Mettre à jour la note moyenne du service
        updateServiceAverageRating(service.getId());

        return savedReview;
    }

    @Override
    public Review updateReview(Long id, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé avec l'ID: " + id));

        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        // Mettre à jour la note moyenne si c'est un avis machine ou service
        if (review.getMachine() != null) {
            updateMachineAverageRating(review.getMachine().getId());
        } else if (review.getService() != null) {
            updateServiceAverageRating(review.getService().getId());
        }

        return updatedReview;
    }

    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé avec l'ID: " + id));

        Long machineId = null;
        Long serviceId = null;

        if (review.getMachine() != null) {
            machineId = review.getMachine().getId();
        } else if (review.getService() != null) {
            serviceId = review.getService().getId();
        }

        reviewRepository.delete(review);

        // Mettre à jour la note moyenne après suppression
        if (machineId != null) {
            updateMachineAverageRating(machineId);
        } else if (serviceId != null) {
            updateServiceAverageRating(serviceId);
        }
    }

    @Override
    public List<Review> getReviewsByMachine(Long machineId) {
        return reviewRepository.findByMachineId(machineId);
    }

    @Override
    public List<Review> getReviewsByService(Long serviceId) {
        return reviewRepository.findByServiceId(serviceId);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public Double getAverageRatingForMachine(Long machineId) {
        Double avg = reviewRepository.getAverageRatingForMachine(machineId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public Double getAverageRatingForService(Long serviceId) {
        Double avg = reviewRepository.getAverageRatingForService(serviceId);
        return avg != null ? avg : 0.0;
    }

    // Méthodes privées pour mettre à jour les notes moyennes
    private void updateMachineAverageRating(Long machineId) {
        Double averageRating = reviewRepository.getAverageRatingForMachine(machineId);
        Integer reviewCount = reviewRepository.getReviewCountForMachine(machineId);

        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine != null) {
            machine.setRating(averageRating != null ? averageRating : 0.0);
            machine.setReviewCount(reviewCount != null ? reviewCount : 0);
            machineRepository.save(machine);
        }
    }

    private void updateServiceAverageRating(Long serviceId) {
        Double averageRating = reviewRepository.getAverageRatingForService(serviceId);
        Integer reviewCount = reviewRepository.getReviewCountForService(serviceId);

        ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);
        if (service != null) {
            service.setRating(averageRating != null ? averageRating : 0.0);
            service.setReviewCount(reviewCount != null ? reviewCount : 0);
            serviceRepository.save(service);
        }
    }
}