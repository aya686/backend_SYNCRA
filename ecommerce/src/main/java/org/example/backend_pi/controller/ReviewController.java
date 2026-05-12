package org.example.backend_pi.controller;
// controller/ReviewController.java
// controller/ReviewController.java

import org.example.backend_pi.entity.Review;
import org.example.backend_pi.service.ReviewService;
import org.example.backend_pi.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Ajouter un avis pour une machine
    @PostMapping("/machine")
    public ResponseEntity<Review> addMachineReview(@RequestBody ReviewDTO reviewDTO) {
        try {
            // Vérifier que machineId est présent
            if (reviewDTO.getMachineId() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            Review review = reviewService.addMachineReview(reviewDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Ajouter un avis pour un service
    @PostMapping("/service")
    public ResponseEntity<Review> addServiceReview(@RequestBody ReviewDTO reviewDTO) {
        try {
            // Vérifier que serviceId est présent
            if (reviewDTO.getServiceId() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            Review review = reviewService.addServiceReview(reviewDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Modifier un avis
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        try {
            Review review = reviewService.updateReview(id, reviewDTO);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supprimer un avis (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok("Avis supprimé définitivement avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        }
    }

    // Récupérer tous les avis d'une machine
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Review>> getReviewsByMachine(@PathVariable Long machineId) {
        List<Review> reviews = reviewService.getReviewsByMachine(machineId);
        return ResponseEntity.ok(reviews);
    }

    // Récupérer tous les avis d'un service
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<Review>> getReviewsByService(@PathVariable Long serviceId) {
        List<Review> reviews = reviewService.getReviewsByService(serviceId);
        return ResponseEntity.ok(reviews);
    }

    // Récupérer les avis d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        List<Review> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    // Récupérer la note moyenne d'une machine
    @GetMapping("/machine/{machineId}/rating")
    public ResponseEntity<Double> getAverageRatingForMachine(@PathVariable Long machineId) {
        Double rating = reviewService.getAverageRatingForMachine(machineId);
        return ResponseEntity.ok(rating);
    }

    // Récupérer la note moyenne d'un service
    @GetMapping("/service/{serviceId}/rating")
    public ResponseEntity<Double> getAverageRatingForService(@PathVariable Long serviceId) {
        Double rating = reviewService.getAverageRatingForService(serviceId);
        return ResponseEntity.ok(rating);
    }
}