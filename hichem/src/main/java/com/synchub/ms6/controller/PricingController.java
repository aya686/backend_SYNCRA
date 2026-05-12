package com.synchub.ms6.controller;

import com.synchub.ms6.dto.PricingDTOs.*;
import com.synchub.ms6.service.DynamicPricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST Controller pour le Pricing Dynamique Algorithmique.
 * Expose les endpoints de calcul d'élasticité, d'optimisation de prix et de simulation.
 */
@RestController
@RequestMapping("/ms6/api/pricing")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class PricingController {

    private final DynamicPricingService pricingService;

    // ==================== CONFIGURATION ====================

    /**
     * Créer une configuration de pricing pour un produit
     */
    @PostMapping("/config")
    public ResponseEntity<PricingConfigurationResponse> createConfiguration(
            @RequestBody PricingConfigurationRequest request) {
        log.info("Création configuration pricing pour produit: {}", request.getProduitId());
        PricingConfigurationResponse response = pricingService.createConfiguration(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer la configuration d'un produit
     */
    @GetMapping("/config/{produitId}")
    public ResponseEntity<PricingConfigurationResponse> getConfiguration(
            @PathVariable Long produitId) {
        PricingConfigurationResponse response = pricingService.getConfiguration(produitId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lister toutes les configurations actives
     */
    @GetMapping("/configs")
    public ResponseEntity<List<PricingConfigurationResponse>> getAllConfigurations() {
        List<PricingConfigurationResponse> configs = pricingService.getAllActiveConfigurations();
        return ResponseEntity.ok(configs);
    }

    // ==================== CALCUL ÉLASTICITÉ ====================

    /**
     * Calculer l'élasticité-prix d'un produit
     * Utilise la régression linéaire logarithmique sur l'historique des données
     */
    @PostMapping("/elasticity/{produitId}")
    public ResponseEntity<ElasticityCalculationResponse> calculateElasticity(
            @PathVariable Long produitId,
            @RequestParam(defaultValue = "30") Integer periodeJours) {
        log.info("Calcul élasticité pour produit: {} (sur {} jours)", produitId, periodeJours);
        ElasticityCalculationResponse response = pricingService.calculateElasticity(produitId, periodeJours);
        return ResponseEntity.ok(response);
    }

    // ==================== CALCUL PRIX OPTIMAL ====================

    /**
     * Calculer le prix optimal pour un produit
     * Basé sur l'élasticité, la demande, le stock et la concurrence
     */
    @PostMapping("/calculate/{produitId}")
    public ResponseEntity<PriceCalculationResponse> calculateOptimalPrice(
            @PathVariable Long produitId,
            @RequestBody PriceCalculationRequest request) {
        // S'assurer que l'ID du path correspond au body
        request.setProduitId(produitId);
        log.info("Calcul prix optimal pour produit: {}", produitId);
        PriceCalculationResponse response = pricingService.calculateOptimalPrice(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Appliquer immédiatement le prix calculé
     */
    @PostMapping("/apply/{produitId}")
    public ResponseEntity<PriceCalculationResponse> applyOptimalPrice(
            @PathVariable Long produitId) {
        log.info("Application prix optimal pour produit: {}", produitId);
        
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .produitId(produitId)
                .appliquerAutomatiquement(true)
                .build();
        
        PriceCalculationResponse response = pricingService.calculateOptimalPrice(request);
        return ResponseEntity.ok(response);
    }

    // ==================== SIMULATION ====================

    /**
     * Simuler l'effet d'un changement de prix
     */
    @PostMapping("/simulate/{produitId}")
    public ResponseEntity<PriceSimulationResponse> simulatePriceChange(
            @PathVariable Long produitId,
            @RequestBody PriceSimulationRequest request) {
        request.setProduitId(produitId);
        log.info("Simulation changement prix pour produit: {} → {} DT", 
                produitId, request.getNouveauPrix());
        
        PriceSimulationResponse response = pricingService.simulatePriceChange(request);
        return ResponseEntity.ok(response);
    }

    // ==================== OPTIMISATION BATCH ====================

    /**
     * Optimiser les prix pour plusieurs produits en une seule opération
     */
    @PostMapping("/optimize-batch")
    public ResponseEntity<BatchPriceOptimizationResponse> optimizeBatch(
            @RequestBody BatchPriceOptimizationRequest request) {
        log.info("Optimisation batch pour {} produits", request.getProduitIds().size());
        BatchPriceOptimizationResponse response = pricingService.optimizeBatch(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint simplifié pour optimiser tous les produits d'une boutique
     */
    @PostMapping("/optimize-boutique/{boutiqueId}")
    public ResponseEntity<BatchPriceOptimizationResponse> optimizeBoutique(
            @PathVariable Long boutiqueId,
            @RequestParam(defaultValue = "false") Boolean appliquer,
            @RequestParam(defaultValue = "1.0") Double seuilVariation) {
        
        log.info("Optimisation tous produits boutique: {}", boutiqueId);
        
        // Note: Dans une vraie implémentation, on récupérerait les produits de la boutique
        // Pour l'exemple, on utilise des IDs factices
        List<Long> produitIds = Arrays.asList(1L, 2L, 3L); // À remplacer par requête réelle
        
        BatchPriceOptimizationRequest request = BatchPriceOptimizationRequest.builder()
                .produitIds(produitIds)
                .appliquerAutomatiquement(appliquer)
                .seuilVariationMin(seuilVariation)
                .build();
        
        BatchPriceOptimizationResponse response = pricingService.optimizeBatch(request);
        return ResponseEntity.ok(response);
    }

    // ==================== ANALYTICS ====================

    /**
     * Récupérer les statistiques de pricing dynamique
     */
    @GetMapping("/analytics")
    public ResponseEntity<PricingAnalyticsDTO> getAnalytics() {
        PricingAnalyticsDTO analytics = pricingService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // ==================== ENDPOINTS D'AIDE ====================

    /**
     * Exemple simple pour tester le calcul d'élasticité avec données générées
     */
    @PostMapping("/demo/calculate-elasticity")
    public ResponseEntity<String> demoCalculateElasticity() {
        String explication = """
            🧮 Comment calculer l'élasticité-prix:
            
            1. Collecter données historiques: (Prix, Quantité vendue) pour différentes périodes
            2. Appliquer transformation logarithmique: ln(Prix) et ln(Quantité)
            3. Régression linéaire: ln(Q) = α + β·ln(P)
            4. β = élasticité-prix
            
            Exemple avec données:
            Période 1: Prix=100, Qté=50 → ln(100)=4.6, ln(50)=3.9
            Période 2: Prix=110, Qté=45 → ln(110)=4.7, ln(45)=3.8
            Période 3: Prix=90, Qté=55  → ln(90)=4.5, ln(55)=4.0
            
            Régression donne β ≈ -1.2 (élastique)
            
            Interprétation:
            • β < -1: Produit élastique (baisse prix → ↑↑ volume)
            • -1 < β < 0: Produit inélastique (hausse prix → peu de perte volume)
            • β ≈ 0: Produit parfaitement inélastique (prix sans effet sur quantité)
            
            Utiliser POST /api/pricing/elasticity/{produitId}?periodeJours=30
            """;
        return ResponseEntity.ok(explication);
    }

    /**
     * Explication des stratégies de pricing
     */
    @GetMapping("/strategies")
    public ResponseEntity<String> getStrategies() {
        String strategies = """
            🎯 Stratégies de Pricing Disponibles:
            
            1️⃣ DYNAMIQUE (par défaut)
               → Ajustements automatiques basés sur algorithmes
               
            2️⃣ STATIQUE
               → Prix fixe, pas d'ajustement
               
            3️⃣ PENETRATION
               → Prix bas initialement pour gagner parts de marché
               → Puis augmentation progressive
               
            4️⃣ SKIMMING
               → Prix haut au lancement (maximiser marge)
               → Puis descente pour capter segments prix
               
            5️⃣ COMPETITIVE
               → Suivi des prix concurrents
               → Matching ou undercutting calculé
               
            6️⃣ ELASTICITE_BASEE
               → Ajustements directement proportionnels à l'élasticité calculée
            """;
        return ResponseEntity.ok(strategies);
    }
}
