package com.synchub.ms6.controller;

import com.synchub.ms6.entity.Produit;
import com.synchub.ms6.ml.model.CollaborativeFilteringRecommender.ProductRecommendation;
import com.synchub.ms6.ml.model.DemandForecaster.*;
import com.synchub.ms6.ml.model.DeliveryDelayPredictor;
import com.synchub.ms6.ml.service.*;
import com.synchub.ms6.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller REST pour les fonctionnalités ML avancées
 * Expose les algorithmes de Machine Learning entraînés sur les données réelles
 */
@RestController
@RequestMapping("/ms6/api/ml")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MLPredictionController {

    private final DeliveryPredictionService deliveryPredictionService;
    private final RecommendationService recommendationService;
    private final DemandForecastingService demandForecastingService;
    private final ProduitRepository produitRepository;

    // ==================== PRÉDICTION DE RETARDS DE LIVRAISON ====================

    /**
     * Prédit le risque de retard pour une livraison future
     * Utilise la régression logistique entraînée sur les données historiques
     */
    @PostMapping("/delivery/predict")
    public ResponseEntity<DeliveryPredictionResponse> predictDeliveryDelay(
            @RequestBody DeliveryPredictionRequest request) {
        
        log.info("Prédiction de retard pour livraison vers {}", request.city());
        
        DeliveryPredictionService.DelayPrediction prediction = 
            deliveryPredictionService.predictForNewDelivery(
                request.city(),
                request.transporteur(),
                request.dateExpedition() != null ? request.dateExpedition() : LocalDateTime.now()
            );
        
        // Analyse des facteurs de risque
        DeliveryPredictionService.RiskAnalysis riskAnalysis = 
            deliveryPredictionService.analyzeRiskFactors(
                request.city(),
                request.transporteur(),
                request.dateExpedition() != null ? request.dateExpedition() : LocalDateTime.now()
            );
        
        DeliveryPredictionResponse response = new DeliveryPredictionResponse(
            (int) Math.round(prediction.probability() * 100),
            prediction.riskLevel().name(),
            prediction.riskLevel().label,
            prediction.riskLevel().description,
            prediction.explanation(),
            riskAnalysis.factors()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques du modèle de prédiction de livraison
     */
    @GetMapping("/delivery/stats")
    public ResponseEntity<Map<String, Object>> getDeliveryModelStats() {
        DeliveryPredictionService.ModelStats stats = 
            deliveryPredictionService.getModelStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("trained", stats.trained());
        response.put("trainingSamples", stats.samples());
        response.put("accuracy", Math.round(stats.accuracy() * 100));
        response.put("status", stats.status());
        response.put("algorithm", "Logistic Regression (from scratch Java implementation)");
        response.put("features", new String[]{
            "temperature", "rain_condition", "snow_condition", "storm_condition",
            "transporteur_encoded", "day_of_week", "season", "estimated_distance"
        });
        
        return ResponseEntity.ok(response);
    }

    // ==================== SYSTÈME DE RECOMMANDATION ====================

    /**
     * Obtient les recommandations personnalisées pour un utilisateur
     * Algorithme: User-Based Collaborative Filtering avec k-NN
     */
    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<RecommendationResponse> getUserRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Récupération des recommandations pour l'utilisateur {}", userId);
        
        List<ProductRecommendation> recommendations = 
            recommendationService.getRecommendationsForUser(userId);
        
        // Limiter si nécessaire
        if (recommendations.size() > limit) {
            recommendations = recommendations.subList(0, limit);
        }
        
        // Enrichir avec les noms de produits
        List<EnrichedRecommendation> enriched = enrichWithProductNames(recommendations);
        
        RecommendationService.RecommendationStats stats = 
            recommendationService.getRecommendationStats();
        
        RecommendationResponse response = new RecommendationResponse(
            userId,
            enriched,
            enriched.size(),
            "Collaborative Filtering (User-Based k-NN)",
            Map.of(
                "modelTrained", stats.modelTrained(),
                "totalUsers", stats.totalUsers(),
                "totalProducts", stats.totalProducts(),
                "totalInteractions", stats.totalInteractions()
            )
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enrichit les recommandations avec les noms de produits
     */
    private List<EnrichedRecommendation> enrichWithProductNames(List<ProductRecommendation> recommendations) {
        List<EnrichedRecommendation> enriched = new ArrayList<>();
        
        for (ProductRecommendation rec : recommendations) {
            Optional<Produit> produit = produitRepository.findById(rec.productId());
            String productName = produit.map(Produit::getNom).orElse("Produit #" + rec.productId());
            
            enriched.add(new EnrichedRecommendation(
                rec.productId(),
                productName,
                rec.score(),
                rec.reason()
            ));
        }
        
        return enriched;
    }

    /**
     * Obtient les produits similaires à un produit donné
     * "Les clients ayant acheté X ont aussi acheté Y"
     */
    @GetMapping("/recommendations/similar/{productId}")
    public ResponseEntity<SimilarProductsResponse> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductRecommendation> similar = 
            recommendationService.getSimilarProducts(productId);
        
        if (similar.size() > limit) {
            similar = similar.subList(0, limit);
        }
        
        // Enrichir avec les noms de produits
        List<EnrichedRecommendation> enriched = enrichWithProductNames(similar);
        
        // Récupérer le nom du produit source
        Optional<Produit> sourceProduct = produitRepository.findById(productId);
        String sourceProductName = sourceProduct.map(Produit::getNom).orElse("Produit #" + productId);
        
        SimilarProductsResponse response = new SimilarProductsResponse(
            productId,
            sourceProductName,
            enriched,
            "Item-Based Collaborative Filtering"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Recommandations basées sur le panier actuel
     */
    @PostMapping("/recommendations/cart")
    public ResponseEntity<CartRecommendationsResponse> getCartBasedRecommendations(
            @RequestBody CartRecommendationsRequest request) {
        
        List<ProductRecommendation> recommendations = 
            recommendationService.getCartBasedRecommendations(request.productIds());
        
        // Enrichir avec les noms de produits
        List<EnrichedRecommendation> enriched = enrichWithProductNames(recommendations);
        
        CartRecommendationsResponse response = new CartRecommendationsResponse(
            request.productIds(),
            enriched,
            "Market Basket Analysis (Association Rules)"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques du système de recommandation
     */
    @GetMapping("/recommendations/stats")
    public ResponseEntity<RecommendationService.RecommendationStats> getRecommendationStats() {
        return ResponseEntity.ok(recommendationService.getRecommendationStats());
    }

    /**
     * Obtient les produits les plus populaires
     * Recommandations basées sur la popularité (nombre d'interactions)
     */
    @GetMapping("/recommendations/popular")
    public ResponseEntity<PopularProductsResponse> getPopularProducts(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Récupération des produits populaires (limit: {})", limit);
        
        List<ProductRecommendation> popular = recommendationService.getPopularProducts();
        
        if (popular.size() > limit) {
            popular = popular.subList(0, limit);
        }
        
        // Enrichir avec les noms de produits
        List<EnrichedRecommendation> enriched = enrichWithProductNames(popular);
        
        PopularProductsResponse response = new PopularProductsResponse(
            enriched,
            enriched.size(),
            "Produits les plus populaires basés sur les interactions utilisateur"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Recommandations basées sur le nom d'un produit
     * Cherche le produit par nom puis retourne les produits similaires
     */
    @GetMapping("/recommendations/by-product-name/{productName}")
    public ResponseEntity<ProductNameRecommendationResponse> getRecommendationsByProductName(
            @PathVariable String productName,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Recherche de recommandations pour le produit: {}", productName);
        
        // Chercher le produit par nom (exact ou contenant)
        Optional<Produit> produitExact = produitRepository.findByNomIgnoreCase(productName);
        List<Produit> produits;
        
        if (produitExact.isPresent()) {
            produits = List.of(produitExact.get());
        } else {
            produits = produitRepository.findByNomContainingIgnoreCase(productName);
        }
        
        if (produits.isEmpty()) {
            return ResponseEntity.ok(new ProductNameRecommendationResponse(
                null,
                productName,
                List.of(),
                0,
                "Aucun produit trouvé avec ce nom"
            ));
        }
        
        // Prendre le premier produit trouvé
        Produit produit = produits.get(0);
        
        // Obtenir les recommandations similaires
        List<ProductRecommendation> recommendations = 
            recommendationService.getSimilarProducts(produit.getProduitId());
        
        if (recommendations.size() > limit) {
            recommendations = recommendations.subList(0, limit);
        }
        
        // Enrichir avec les noms de produits
        List<EnrichedRecommendation> enriched = enrichWithProductNames(recommendations);
        
        ProductNameRecommendationResponse response = new ProductNameRecommendationResponse(
            produit.getProduitId(),
            produit.getNom(),
            enriched,
            enriched.size(),
            enriched.isEmpty() ? "Aucune recommandation similaire trouvée" : "Success"
        );
        
        return ResponseEntity.ok(response);
    }

    // ==================== PRÉVISION DE DEMANDE (TIME SERIES) ====================

    /**
     * Prédit la demande pour un produit sur les prochains jours
     * Algorithme: Holt-Winters Triple Exponential Smoothing
     */
    @GetMapping("/demand/forecast/{productId}")
    public ResponseEntity<DemandForecastResponse> getDemandForecast(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Prévision de demande pour le produit {} sur {} jours", productId, days);
        
        List<ForecastPoint> forecasts = demandForecastingService.getDemandForecast(productId, days);
        
        if (forecasts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Analyse de tendance
        TrendAnalysis trend = demandForecastingService.getTrendAnalysis(productId);
        
        // Rapport complet
        DemandForecastingService.ForecastReport report = 
            demandForecastingService.generateForecastReport(productId);
        
        double totalDemand = forecasts.stream()
                .mapToDouble(ForecastPoint::predictedValue)
                .sum();
        
        DemandForecastResponse response = new DemandForecastResponse(
            productId,
            report.produitNom(),
            forecasts,
            trend,
            totalDemand,
            report.currentStock(),
            report.stockRisks(),
            report.recommendation(),
            "Holt-Winters Triple Exponential Smoothing (from scratch Java)"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Analyse les risques de rupture de stock pour tous les produits
     */
    @GetMapping("/demand/stock-risks")
    public ResponseEntity<List<DemandForecastingService.StockRiskAlert>> getAllStockRisks() {
        List<DemandForecastingService.StockRiskAlert> risks = 
            demandForecastingService.analyzeAllStockRisks();
        
        return ResponseEntity.ok(risks);
    }

    /**
     * Analyse le risque de rupture pour un produit spécifique
     */
    @GetMapping("/demand/stock-risk/{productId}")
    public ResponseEntity<Map<String, Object>> getProductStockRisk(
            @PathVariable Long productId) {
        
        // Simulation - en vrai, récupérer le stock actuel
        int currentStock = 100; // Placeholder
        
        List<StockRisk> risks = demandForecastingService.analyzeStockRisk(productId, currentStock);
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("currentStock", currentStock);
        response.put("risks", risks);
        response.put("riskLevel", risks.isEmpty() ? "LOW" : risks.get(0).level().name());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les produits avec les ventes prévues les plus élevées
     */
    @GetMapping("/demand/top-predicted")
    public ResponseEntity<List<DemandForecastingService.ProductForecastSummary>> getTopPredictedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<DemandForecastingService.ProductForecastSummary> products = 
            demandForecastingService.getTopPredictedProducts(limit);
        
        return ResponseEntity.ok(products);
    }

    /**
     * Calcule le niveau de stock optimal pour un produit
     */
    @GetMapping("/demand/optimal-stock/{productId}")
    public ResponseEntity<Map<String, Object>> getOptimalStockLevel(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "7") int leadTimeDays,
            @RequestParam(defaultValue = "20") double safetyStockPercent) {
        
        double optimalStock = demandForecastingService.calculateOptimalStockLevel(
            productId, leadTimeDays, safetyStockPercent);
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("optimalStockLevel", Math.round(optimalStock));
        response.put("leadTimeDays", leadTimeDays);
        response.put("safetyStockPercent", safetyStockPercent);
        response.put("formula", "Lead Time Demand + Safety Stock");
        
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN/ENTRAÎNEMENT ====================

    /**
     * Force l'entraînement manuel de tous les modèles ML
     * Utile pour les tests et la démonstration
     */
    @PostMapping("/admin/retrain")
    public ResponseEntity<Map<String, Object>> forceRetrain() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Entraîner le modèle de prédiction de livraison
            deliveryPredictionService.retrainModel();
            results.put("deliveryPrediction", "Entraînement démarré");
        } catch (Exception e) {
            results.put("deliveryPrediction", "Erreur: " + e.getMessage());
        }
        
        try {
            // Entraîner le système de recommandation
            recommendationService.retrainModel();
            results.put("recommendation", "Entraînement démarré");
        } catch (Exception e) {
            results.put("recommendation", "Erreur: " + e.getMessage());
        }
        
        try {
            // Entraîner les modèles de prévision
            demandForecastingService.retrainAllModels();
            results.put("demandForecasting", "Entraînement démarré");
        } catch (Exception e) {
            results.put("demandForecasting", "Erreur: " + e.getMessage());
        }
        
        results.put("message", "Entraînement des modèles ML lancé manuellement");
        results.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(results);
    }

    // ==================== STATISTIQUES GLOBALES ML ====================

    /**
     * Obtient un résumé de toutes les capacités ML
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMLStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Stats de prédiction de livraison
        stats.put("deliveryPrediction", Map.of(
            "algorithm", "Logistic Regression (from scratch)",
            "features", 8,
            "trainingMethod", "Gradient Descent with L2 Regularization",
            "stats", deliveryPredictionService.getModelStatistics()
        ));
        
        // Stats de recommandation
        RecommendationService.RecommendationStats recStats = 
            recommendationService.getRecommendationStats();
        stats.put("recommendation", Map.of(
            "algorithm", "Collaborative Filtering (User-Based k-NN)",
            "similarityMetric", "Cosine Similarity",
            "modelTrained", recStats.modelTrained(),
            "totalUsers", recStats.totalUsers(),
            "totalProducts", recStats.totalProducts(),
            "totalInteractions", recStats.totalInteractions()
        ));
        
        // Stats de prévision de demande
        stats.put("demandForecasting", Map.of(
            "algorithm", "Holt-Winters Triple Exponential Smoothing",
            "components", new String[]{"Level", "Trend", "Seasonality"},
            "optimization", "Grid Search for alpha/beta/gamma",
            "seasonalityPeriod", "7 days (weekly)"
        ));
        
        return ResponseEntity.ok(stats);
    }

    // ==================== RECORDS POUR LES REQUÊTES/RÉPONSES ====================

    public record DeliveryPredictionRequest(
        String city,
        String transporteur,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateExpedition
    ) {}

    public record DeliveryPredictionResponse(
        int probability,
        String riskLevel,
        String riskLabel,
        String riskDescription,
        String explanation,
        List<DeliveryPredictionService.RiskFactor> riskFactors
    ) {}

    public record RecommendationResponse(
        Long userId,
        List<EnrichedRecommendation> recommendations,
        int count,
        String algorithm,
        Map<String, Object> modelStats
    ) {}

    public record SimilarProductsResponse(
        Long productId,
        String productName,
        List<EnrichedRecommendation> similarProducts,
        String algorithm
    ) {}

    public record CartRecommendationsRequest(List<Long> productIds) {}

    public record CartRecommendationsResponse(
        List<Long> cartProducts,
        List<EnrichedRecommendation> recommendations,
        String algorithm
    ) {}

    public record DemandForecastResponse(
        Long productId,
        String productName,
        List<ForecastPoint> forecasts,
        TrendAnalysis trendAnalysis,
        double totalPredictedDemand,
        double currentStock,
        List<StockRisk> stockRisks,
        String recommendation,
        String algorithm
    ) {}

    public record ProductNameRecommendationResponse(
        Long productId,
        String productName,
        List<EnrichedRecommendation> recommendations,
        int count,
        String message
    ) {}

    public record PopularProductsResponse(
        List<EnrichedRecommendation> products,
        int count,
        String description
    ) {}

    public record EnrichedRecommendation(
        Long productId,
        String productName,
        double score,
        String reason
    ) {}
}
