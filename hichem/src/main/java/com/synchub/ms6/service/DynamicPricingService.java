package com.synchub.ms6.service;

import com.synchub.ms6.dto.PricingDTOs;
import com.synchub.ms6.dto.PricingDTOs.*;
import com.synchub.ms6.entity.DemandData;
import com.synchub.ms6.entity.PriceHistory;
import com.synchub.ms6.entity.PricingConfiguration;
import com.synchub.ms6.entity.Produit;
import com.synchub.ms6.repository.DemandDataRepository;
import com.synchub.ms6.repository.PriceHistoryRepository;
import com.synchub.ms6.repository.PricingConfigurationRepository;
import com.synchub.ms6.repository.ProduitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicPricingService {

    private final PricingConfigurationRepository configRepository;
    private final PriceHistoryRepository historyRepository;
    private final DemandDataRepository demandRepository;
    private final ProduitRepository produitRepository;
    private final PricingEngine pricingEngine;

    // ==================== GESTION CONFIGURATION ====================

    @Transactional
    public PricingConfigurationResponse createConfiguration(PricingConfigurationRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + request.getProduitId()));

        PricingConfiguration config = PricingConfiguration.builder()
                .produit(produit)
                .prixBase(request.getPrixBase())
                .prixMin(request.getPrixMin())
                .prixMax(request.getPrixMax())
                .strategie(request.getStrategie() != null ? request.getStrategie() : PricingConfiguration.StrategiePricing.DYNAMIQUE)
                .coefficientDemande(request.getCoefficientDemande() != null ? request.getCoefficientDemande() : 1.0)
                .coefficientConcurrence(request.getCoefficientConcurrence() != null ? request.getCoefficientConcurrence() : 1.0)
                .coefficientSaisonnalite(request.getCoefficientSaisonnalite() != null ? request.getCoefficientSaisonnalite() : 1.0)
                .actif(true)
                .build();

        PricingConfiguration saved = configRepository.save(config);
        log.info("Configuration pricing créée pour produit {}: {} (stratégie: {})",
                produit.getNom(), saved.getPrixBase(), saved.getStrategie());

        return convertToConfigResponse(saved);
    }

    @Transactional(readOnly = true)
    public PricingConfigurationResponse getConfiguration(Long produitId) {
        PricingConfiguration config = configRepository.findFirstByProduitProduitIdOrderByDateCreationDesc(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Configuration non trouvée pour produit: " + produitId));
        return convertToConfigResponse(config);
    }

    @Transactional(readOnly = true)
    public List<PricingConfigurationResponse> getAllActiveConfigurations() {
        return configRepository.findByActifTrue().stream()
                .map(this::convertToConfigResponse)
                .collect(Collectors.toList());
    }

    // ==================== CALCUL ÉLASTICITÉ ====================

    @Transactional
    public ElasticityCalculationResponse calculateElasticity(Long produitId, Integer periodeJours) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + produitId));

        LocalDateTime since = LocalDateTime.now().minusDays(periodeJours != null ? periodeJours : 30);
        List<DemandData> dataPoints = demandRepository.findRecentDemandData(produitId, since);

        if (dataPoints.size() < 3) {
            return ElasticityCalculationResponse.builder()
                    .produitId(produitId)
                    .nomProduit(produit.getNom())
                    .elasticiteCalculee(null)
                    .interpretation("Pas assez de données historiques (minimum 3 points requis, trouvé: " + dataPoints.size() + ")")
                    .formuleUtilisee("ln(Q) = α + β·ln(P)")
                    .build();
        }

        Double elasticity = pricingEngine.calculateElasticity(dataPoints);
        
        String interpretation;
        if (elasticity == null) {
            interpretation = "Impossible de calculer l'élasticité avec les données disponibles";
        } else if (elasticity < -1.5) {
            interpretation = String.format("Élasticité très élevée (%.2f): le produit est très sensible aux prix. " +
                    "Une baisse de prix augmentera significativement la quantité vendue.", elasticity);
        } else if (elasticity < -1.0) {
            interpretation = String.format("Élasticité élevée (%.2f): le produit est élastique. " +
                    "Privilégier le volume sur la marge.", elasticity);
        } else if (elasticity < -0.5) {
            interpretation = String.format("Élasticité modérée (%.2f): équilibre entre volume et marge possible.", elasticity);
        } else {
            interpretation = String.format("Élasticité faible (%.2f): le produit est inélastique. " +
                    "On peut augmenter les prix sans perte de volume significative.", elasticity);
        }

        // Calculer R² (coefficient de détermination simplifié)
        Double rSquared = calculateRSquared(dataPoints, elasticity);

        // Sauvegarder l'élasticité dans la configuration (la plus récente)
        configRepository.findFirstByProduitProduitIdOrderByDateCreationDesc(produitId).ifPresent(config -> {
            config.setElasticitePrix(elasticity);
            configRepository.save(config);
        });

        List<DataPoint> points = dataPoints.stream()
                .map(d -> DataPoint.builder()
                        .date(d.getDatePeriode())
                        .prix(d.getPrixApplique())
                        .quantite(d.getQuantiteVendue())
                        .build())
                .collect(Collectors.toList());

        return ElasticityCalculationResponse.builder()
                .produitId(produitId)
                .nomProduit(produit.getNom())
                .elasticiteCalculee(elasticity)
                .interpretation(interpretation)
                .pointsDonnees(points)
                .coefficientDetermination(rSquared)
                .formuleUtilisee("Régression linéaire logarithmique: ln(Q) = α + β·ln(P) où β = élasticité")
                .build();
    }

    // ==================== CALCUL PRIX OPTIMAL ====================

    @Transactional
    public PriceCalculationResponse calculateOptimalPrice(PriceCalculationRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + request.getProduitId()));

        PricingConfiguration config = configRepository.findFirstByProduitProduitIdOrderByDateCreationDesc(request.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("Configuration pricing non trouvée"));

        Double prixActuel = request.getPrixActuel() != null ? request.getPrixActuel() : produit.getPrix();
        Double prixBase = config.getPrixBase();
        Double elasticite = config.getElasticitePrix();

        // Calculer facteurs
        Double facteurDemande = calculateDemandFactor(request, config);
        Double facteurConcurrence = request.getPrixConcurrent() != null && prixActuel > 0 
                ? request.getPrixConcurrent() / prixActuel 
                : config.getCoefficientConcurrence();
        Double niveauStock = request.getStockDisponible() != null 
                ? Math.min(1.0, request.getStockDisponible() / 100.0) // Normaliser
                : 0.5;

        // Calculer prix optimal
        Double prixOptimal = pricingEngine.calculateOptimalPrice(
                prixBase,
                elasticite,
                facteurDemande,
                facteurConcurrence,
                niveauStock,
                config.getPrixMin(),
                config.getPrixMax()
        );

        if (prixOptimal == null) {
            return PriceCalculationResponse.builder()
                    .produitId(request.getProduitId())
                    .nomProduit(produit.getNom())
                    .prixActuel(prixActuel)
                    .appliquable(false)
                    .message("Impossible de calculer le prix optimal")
                    .build();
        }

        // Calculer estimations
        Integer quantiteRef = 10; // Valeur par défaut, idéalement historique
        Integer quantiteEstimeeAncienPrix = pricingEngine.estimateQuantityAtPrice(quantiteRef, prixBase, prixActuel, elasticite);
        Integer quantiteEstimeeNouveauPrix = pricingEngine.estimateQuantityAtPrice(quantiteRef, prixBase, prixOptimal, elasticite);

        Double revenuAncien = pricingEngine.estimateRevenue(quantiteEstimeeAncienPrix, prixActuel);
        Double revenuNouveau = pricingEngine.estimateRevenue(quantiteEstimeeNouveauPrix, prixOptimal);
        Double gainPotentiel = revenuNouveau - revenuAncien;

        Double variation = ((prixOptimal - prixActuel) / prixActuel) * 100;

        // Déterminer raison
        String raison = determineReason(facteurDemande, niveauStock, facteurConcurrence, elasticite);

        List<String> facteurs = new ArrayList<>();
        if (facteurDemande > 1.1) facteurs.add("Forte demande");
        if (facteurDemande < 0.9) facteurs.add("Faible demande");
        if (niveauStock > 0.8) facteurs.add("Stock élevé");
        if (niveauStock < 0.2) facteurs.add("Stock faible");
        if (facteurConcurrence < 0.95) facteurs.add("Concurrence chère");
        if (facteurConcurrence > 1.05) facteurs.add("Concurrence moins chère");
        if (elasticite != null && elasticite > -0.8) facteurs.add("Produit inélastique");

        // Appliquer si demandé
        if (request.getAppliquerAutomatiquement() != null && request.getAppliquerAutomatiquement()) {
            applyNewPrice(produit, prixOptimal, PriceHistory.RaisonChangement.ELASTICITE_OPTIMISEE, 
                    "Calcul automatique basé sur élasticité");
        }

        return PriceCalculationResponse.builder()
                .produitId(request.getProduitId())
                .nomProduit(produit.getNom())
                .prixActuel(prixActuel)
                .prixCalcule(prixOptimal)
                .variationPercent(variation)
                .raisonAjustement(raison)
                .elasticiteEstimee(elasticite)
                .revenuEstimeAncienPrix(revenuAncien)
                .revenuEstimeNouveauPrix(revenuNouveau)
                .gainRevenuPotentiel(gainPotentiel)
                .facteursConsideres(facteurs)
                .appliquable(Math.abs(variation) >= 1.0) // Minimum 1% de variation
                .message("Prix optimal calculé avec succès")
                .build();
    }

    // ==================== SIMULATION PRIX ====================

    @Transactional(readOnly = true)
    public PriceSimulationResponse simulatePriceChange(PriceSimulationRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + request.getProduitId()));

        PricingConfiguration config = configRepository.findFirstByProduitProduitIdOrderByDateCreationDesc(request.getProduitId())
                .orElse(null);

        Double prixActuel = produit.getPrix();
        Double prixSimule = request.getNouveauPrix();
        Double elasticite = config != null && config.getElasticitePrix() != null ? config.getElasticitePrix() : -1.0;
        Double prixBase = config != null && config.getPrixBase() != null ? config.getPrixBase() : prixActuel;

        Integer quantiteRef = 10;
        Integer qteAncien = pricingEngine.estimateQuantityAtPrice(quantiteRef, prixBase, prixActuel, elasticite);
        Integer qteNouveau = pricingEngine.estimateQuantityAtPrice(quantiteRef, prixBase, prixSimule, elasticite);

        Double revenuAncien = pricingEngine.estimateRevenue(qteAncien, prixActuel);
        Double revenuNouveau = pricingEngine.estimateRevenue(qteNouveau, prixSimule);
        Double marge = revenuNouveau - revenuAncien;

        String recommandation;
        if (marge > 0) {
            recommandation = "✅ Recommandé: gain de revenu estimé à " + String.format("%.2f", marge) + " DT";
        } else if (marge > -50) {
            recommandation = "⚠️ Neutre: impact minime sur le revenu";
        } else {
            recommandation = "❌ Déconseillé: perte de revenu estimée à " + String.format("%.2f", Math.abs(marge)) + " DT";
        }

        return PriceSimulationResponse.builder()
                .produitId(request.getProduitId())
                .nomProduit(produit.getNom())
                .prixActuel(prixActuel)
                .prixSimule(prixSimule)
                .variationPercent(((prixSimule - prixActuel) / prixActuel) * 100)
                .quantiteEstimeeAncienPrix(qteAncien)
                .quantiteEstimeeNouveauPrix(qteNouveau)
                .revenuAncienPrix(revenuAncien)
                .revenuNouveauPrix(revenuNouveau)
                .margeEstimee(marge)
                .recommandation(recommandation)
                .build();
    }

    // ==================== OPTIMISATION BATCH ====================

    @Transactional
    public BatchPriceOptimizationResponse optimizeBatch(BatchPriceOptimizationRequest request) {
        List<PriceCalculationResponse> resultats = new ArrayList<>();
        int optimises = 0;
        int rejetes = 0;
        double gainTotal = 0;

        for (Long produitId : request.getProduitIds()) {
            try {
                Produit produit = produitRepository.findById(produitId).orElse(null);
                if (produit == null) {
                    rejetes++;
                    continue;
                }

                PriceCalculationResponse result = calculateOptimalPrice(PriceCalculationRequest.builder()
                        .produitId(produitId)
                        .prixActuel(produit.getPrix())
                        .appliquerAutomatiquement(request.getAppliquerAutomatiquement())
                        .build());

                resultats.add(result);

                if (result.getAppliquable() != null && result.getAppliquable()) {
                    double seuil = request.getSeuilVariationMin() != null ? request.getSeuilVariationMin() : 1.0;
                    if (Math.abs(result.getVariationPercent()) >= seuil) {
                        optimises++;
                        if (result.getGainRevenuPotentiel() != null) {
                            gainTotal += result.getGainRevenuPotentiel();
                        }
                    }
                } else {
                    rejetes++;
                }

            } catch (Exception e) {
                log.error("Erreur optimisation produit {}: {}", produitId, e.getMessage());
                rejetes++;
            }
        }

        return BatchPriceOptimizationResponse.builder()
                .totalProduits(request.getProduitIds().size())
                .produitsOptimises(optimises)
                .produitsRejetes(rejetes)
                .resultats(resultats)
                .gainRevenuTotalEstime(gainTotal)
                .build();
    }

    // ==================== APPLICATION PRIX ====================

    @Transactional
    public void applyNewPrice(Long produitId, Double nouveauPrix, PriceHistory.RaisonChangement raison, String facteur) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + produitId));

        applyNewPrice(produit, nouveauPrix, raison, facteur);
    }

    private void applyNewPrice(Produit produit, Double nouveauPrix, PriceHistory.RaisonChangement raison, String facteur) {
        Double ancienPrix = produit.getPrix();
        
        // Sauvegarder dans l'historique
        PriceHistory history = PriceHistory.builder()
                .produit(produit)
                .prixAvant(ancienPrix)
                .prixApres(nouveauPrix)
                .raison(raison)
                .facteurDeclencheur(facteur)
                .build();
        historyRepository.save(history);

        // Mettre à jour le produit
        produit.setPrix(nouveauPrix);
        produitRepository.save(produit);

        log.info("Prix appliqué pour {}: {} DT → {} DT (raison: {})",
                produit.getNom(), ancienPrix, nouveauPrix, raison);
    }

    // ==================== ANALYTICS ====================

    @Transactional(readOnly = true)
    public PricingAnalyticsDTO getAnalytics() {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        
        int totalDynamiques = configRepository.findByActifTrue().size();
        
        List<PriceHistory> recentChanges = historyRepository.findRecentChanges(0L, since24h); // 0L pour tous les produits
        int totalChangements = recentChanges.size();
        
        Double avgVariation = historyRepository.calculateAverageVariation(0L, since24h);
        
        // Top augmentations et réductions
        List<TopVariationDTO> topAugmentations = recentChanges.stream()
                .filter(h -> h.getVariationPercent() != null && h.getVariationPercent() > 0)
                .sorted((h1, h2) -> Double.compare(h2.getVariationPercent(), h1.getVariationPercent()))
                .limit(5)
                .map(h -> TopVariationDTO.builder()
                        .produitId(h.getProduit().getProduitId())
                        .nomProduit(h.getProduit().getNom())
                        .ancienPrix(h.getPrixAvant())
                        .nouveauPrix(h.getPrixApres())
                        .variationPercent(h.getVariationPercent())
                        .build())
                .collect(Collectors.toList());

        List<TopVariationDTO> topReductions = recentChanges.stream()
                .filter(h -> h.getVariationPercent() != null && h.getVariationPercent() < 0)
                .sorted((h1, h2) -> Double.compare(h1.getVariationPercent(), h2.getVariationPercent()))
                .limit(5)
                .map(h -> TopVariationDTO.builder()
                        .produitId(h.getProduit().getProduitId())
                        .nomProduit(h.getProduit().getNom())
                        .ancienPrix(h.getPrixAvant())
                        .nouveauPrix(h.getPrixApres())
                        .variationPercent(h.getVariationPercent())
                        .build())
                .collect(Collectors.toList());

        return PricingAnalyticsDTO.builder()
                .totalProduitsDynamiques(totalDynamiques)
                .totalChangements24h(totalChangements)
                .variationPrixMoyenne(avgVariation != null ? avgVariation : 0.0)
                .gainRevenuTotal(0.0) // À calculer avec données réelles
                .topAugmentations(topAugmentations)
                .topReductions(topReductions)
                .build();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Double calculateDemandFactor(PriceCalculationRequest request, PricingConfiguration config) {
        if (request.getVentesDerniereHeure() != null && request.getVuesDerniereHeure() != null 
                && request.getVuesDerniereHeure() > 0) {
            double tauxConversion = (double) request.getVentesDerniereHeure() / request.getVuesDerniereHeure();
            // Normaliser: > 10% = forte demande, < 2% = faible demande
            return 0.8 + (tauxConversion * 4); // Multiplicateur basé sur conversion
        }
        return config.getCoefficientDemande();
    }

    private String determineReason(Double facteurDemande, Double niveauStock, 
                                   Double facteurConcurrence, Double elasticite) {
        if (niveauStock > 0.8) {
            return "Stock élevé: baisse de prix pour stimuler les ventes";
        } else if (niveauStock < 0.2 && facteurDemande > 1.2) {
            return "Stock faible + forte demande: augmentation justifiée";
        } else if (facteurConcurrence > 1.05) {
            return "Concurrence agressive: ajustement compétitif";
        } else if (facteurDemande > 1.3) {
            return "Forte demande: optimisation revenu";
        } else if (elasticite != null && elasticite < -1.2) {
            return "Produit élastique: ajustement pour maximiser volume";
        }
        return "Ajustement dynamique basé sur les conditions de marché";
    }

    private Double calculateRSquared(List<DemandData> dataPoints, Double elasticity) {
        if (elasticity == null || dataPoints == null || dataPoints.size() < 2) return null;
        
        // Filtrer les points valides (prix et quantité > 0)
        List<DemandData> validPoints = dataPoints.stream()
                .filter(d -> d != null)
                .filter(d -> d.getPrixApplique() != null && d.getPrixApplique() > 0)
                .filter(d -> d.getQuantiteVendue() != null && d.getQuantiteVendue() > 0)
                .toList();
        
        if (validPoints.size() < 2) return null;
        
        // Calculer alpha (intercept) pour ln(Q) = alpha + elasticity * ln(P)
        double sumLnQ = 0, sumLnP = 0, n = validPoints.size();
        for (DemandData d : validPoints) {
            sumLnQ += Math.log(d.getQuantiteVendue());
            sumLnP += Math.log(d.getPrixApplique());
        }
        double meanLnQ = sumLnQ / n;
        double meanLnP = sumLnP / n;
        double alpha = meanLnQ - elasticity * meanLnP;
        
        // Calcul du R²
        double ssTotal = validPoints.stream()
                .mapToDouble(d -> Math.pow(Math.log(d.getQuantiteVendue()) - meanLnQ, 2))
                .sum();
        
        double ssResidual = validPoints.stream()
                .mapToDouble(d -> {
                    double predicted = alpha + Math.log(d.getPrixApplique()) * elasticity;
                    return Math.pow(Math.log(d.getQuantiteVendue()) - predicted, 2);
                })
                .sum();
        
        return ssTotal > 0 ? Math.max(0, Math.min(1, 1 - (ssResidual / ssTotal))) : null;
    }

    private PricingConfigurationResponse convertToConfigResponse(PricingConfiguration config) {
        return PricingConfigurationResponse.builder()
                .configId(config.getConfigId())
                .produitId(config.getProduit().getProduitId())
                .nomProduit(config.getProduit().getNom())
                .prixBase(config.getPrixBase())
                .prixMin(config.getPrixMin())
                .prixMax(config.getPrixMax())
                .strategie(config.getStrategie())
                .elasticitePrix(config.getElasticitePrix())
                .coefficientDemande(config.getCoefficientDemande())
                .coefficientConcurrence(config.getCoefficientConcurrence())
                .coefficientSaisonnalite(config.getCoefficientSaisonnalite())
                .actif(config.getActif())
                .dateCreation(config.getDateCreation())
                .build();
    }
}
