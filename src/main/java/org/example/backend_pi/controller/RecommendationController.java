package org.example.backend_pi.controller;

import org.example.backend_pi.dto.RecommendationCriteriaDTO;
import org.example.backend_pi.dto.ScoredResultDTO;
import org.example.backend_pi.service.RecommendationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationEngine recommendationEngine;

    /**
     * POST /api/recommendations
     * Corps : RecommendationCriteriaDTO
     * Retourne : liste de ScoredResultDTO triée par score décroissant
     *
     * Exemple de body :
     * {
     *   "resourceType": "MACHINE",
     *   "machineType": "EQUIPMENT",
     *   "category": "INDUSTRIELLE",
     *   "transactionType": "SALE",
     *   "maxBudget": 80000,
     *   "preferredLocation": "Tunis",
     *   "minRating": 3.5,
     *   "requiresAvailability": true,
     *   "requiredQuantity": 2
     * }
     */
    @PostMapping
    public ResponseEntity<List<ScoredResultDTO>> getRecommendations(
            @RequestBody RecommendationCriteriaDTO criteria) {
        try {
            List<ScoredResultDTO> results = recommendationEngine.recommend(criteria);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/recommendations/quick?resourceType=MACHINE&location=Tunis&maxBudget=50000
     * Version simplifiée pour les filtres rapides depuis l'URL
     */
    @GetMapping("/quick")
    public ResponseEntity<List<ScoredResultDTO>> getQuickRecommendations(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String machineType,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean requiresAvailability,
            @RequestParam(required = false) Integer requiredQuantity) {
        try {
            RecommendationCriteriaDTO criteria = new RecommendationCriteriaDTO();
            criteria.setResourceType(resourceType);
            criteria.setCategory(category);
            criteria.setMachineType(machineType);
            criteria.setServiceType(serviceType);
            criteria.setTransactionType(transactionType);
            criteria.setMaxBudget(maxBudget);
            criteria.setMinBudget(minBudget);
            criteria.setPreferredLocation(location);
            criteria.setMinRating(minRating);
            criteria.setRequiresAvailability(requiresAvailability);
            criteria.setRequiredQuantity(requiredQuantity);

            List<ScoredResultDTO> results = recommendationEngine.recommend(criteria);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}