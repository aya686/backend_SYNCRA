package com.synchub.ms6.service;

import com.synchub.ms6.dto.PricingDTOs;
import com.synchub.ms6.entity.DemandData;
import com.synchub.ms6.entity.PricingConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de calcul algorithmique pour le pricing dynamique.
 * Implémente les algorithmes d'élasticité-prix et d'optimisation.
 */
@Component
@Slf4j
public class PricingEngine {

    // Constantes pour l'optimisation
    private static final double ALPHA_ELASTICITE = 0.4;  // Poids de l'élasticité
    private static final double ALPHA_DEMANDE = 0.3;     // Poids de la demande
    private static final double ALPHA_CONCURRENCE = 0.2; // Poids de la concurrence
    private static final double ALPHA_STOCK = 0.1;       // Poids du stock

    /**
     * Calcule l'élasticité-prix par régression linéaire logarithmique.
     * Formule: ln(Q) = α + β·ln(P) → β = élasticité
     * 
     * @param dataPoints Points de données (prix, quantité)
     * @return Coefficient d'élasticité
     */
    public Double calculateElasticity(List<DemandData> dataPoints) {
        if (dataPoints == null || dataPoints.size() < 3) {
            log.warn("Pas assez de données pour calculer l'élasticité (minimum 3 points)");
            return null;
        }

        // Filtrer les points valides
        List<DemandData> validPoints = dataPoints.stream()
                .filter(d -> d.getPrixApplique() != null && d.getPrixApplique() > 0)
                .filter(d -> d.getQuantiteVendue() != null && d.getQuantiteVendue() > 0)
                .toList();

        if (validPoints.size() < 3) {
            log.warn("Pas assez de points valides après filtrage");
            return null;
        }

        // Régression linéaire sur ln(prix) et ln(quantité)
        int n = validPoints.size();
        double sumLnP = 0, sumLnQ = 0, sumLnPLnQ = 0, sumLnP2 = 0;

        for (DemandData point : validPoints) {
            double lnP = Math.log(point.getPrixApplique());
            double lnQ = Math.log(point.getQuantiteVendue());
            
            sumLnP += lnP;
            sumLnQ += lnQ;
            sumLnPLnQ += lnP * lnQ;
            sumLnP2 += lnP * lnP;
        }

        // Formule élasticité (pente de la régression)
        double denominator = (n * sumLnP2) - (sumLnP * sumLnP);
        if (denominator == 0) {
            log.warn("Dénominateur nul dans le calcul de l'élasticité");
            return null;
        }

        double elasticity = ((n * sumLnPLnQ) - (sumLnP * sumLnQ)) / denominator;
        
        log.info("Élasticité calculée: {} (sur {} points)", String.format("%.4f", elasticity), n);
        return elasticity;
    }

    /**
     * Calcule le prix optimal basé sur l'élasticité et les facteurs de marché.
     * Utilise une optimisation multi-critères pondérée.
     * 
     * @param prixBase Prix de référence
     * @param elasticite Coefficient d'élasticité (négatif normalement)
     * @param facteurDemande Multiplicateur de demande (> 1 = forte demande)
     * @param facteurConcurrence Ratio prix concurrent / prix actuel
     * @param niveauStock Niveau de stock (0-1, 1 = plein)
     * @param prixMin Prix minimum acceptable
     * @param prixMax Prix maximum acceptable
     * @return Prix optimal calculé
     */
    public Double calculateOptimalPrice(
            Double prixBase,
            Double elasticite,
            Double facteurDemande,
            Double facteurConcurrence,
            Double niveauStock,
            Double prixMin,
            Double prixMax) {

        if (prixBase == null || prixBase <= 0) {
            return null;
        }

        // Initialiser les valeurs par défaut
        double elas = (elasticite != null) ? elasticite : -1.0;
        double fDemande = (facteurDemande != null) ? facteurDemande : 1.0;
        double fConcurrence = (facteurConcurrence != null) ? facteurConcurrence : 1.0;
        double fStock = (niveauStock != null) ? niveauStock : 0.5;

        double ajustementTotal = 0.0;

        // 1. Ajustement basé sur l'élasticité
        if (elas < 0) {
            // Produit élastique: forte réaction aux prix
            // Si elasticité < -1 (élastique), on baisse le prix pour augmenter volume
            // Si -1 < elasticité < 0 (inelastique), on augmente le prix pour max marge
            if (elas < -1.0) {
                // Élastique: baisse prix légèrement pour boost volume
                ajustementTotal += ALPHA_ELASTICITE * (-0.05); // -5%
            } else {
                // Inélastique: augmentation possible sans perte volume
                ajustementTotal += ALPHA_ELASTICITE * 0.08; // +8%
            }
        }

        // 2. Ajustement basé sur la demande
        if (fDemande > 1.2) {
            // Forte demande: on peut augmenter le prix
            ajustementTotal += ALPHA_DEMANDE * Math.min((fDemande - 1) * 0.2, 0.15);
        } else if (fDemande < 0.8) {
            // Faible demande: stimulation par baisse de prix
            ajustementTotal -= ALPHA_DEMANDE * Math.min((1 - fDemande) * 0.3, 0.20);
        }

        // 3. Ajustement basé sur la concurrence
        if (fConcurrence < 0.95) {
            // Concurrence plus chère: on peut légèrement augmenter
            ajustementTotal += ALPHA_CONCURRENCE * 0.03;
        } else if (fConcurrence > 1.05) {
            // Concurrence moins chère: matching prix nécessaire
            ajustementTotal -= ALPHA_CONCURRENCE * 0.05;
        }

        // 4. Ajustement basé sur le stock
        if (fStock > 0.8) {
            // Stock élevé: promotion pour désendettement
            ajustementTotal -= ALPHA_STOCK * 0.10;
        } else if (fStock < 0.2) {
            // Stock faible + forte demande: augmentation justifiée
            ajustementTotal += ALPHA_STOCK * 0.15;
        }

        // Calculer le prix optimal
        double prixOptimal = prixBase * (1 + ajustementTotal);

        // Contraintes min/max
        double min = (prixMin != null) ? prixMin : prixBase * 0.7;
        double max = (prixMax != null) ? prixMax : prixBase * 1.3;

        prixOptimal = Math.max(min, Math.min(max, prixOptimal));

        log.info("Prix optimal calculé: {} (base: {}, ajustement: {}%)", 
                String.format("%.2f", prixOptimal), 
                String.format("%.2f", prixBase),
                String.format("%.2f", ajustementTotal * 100));

        return prixOptimal;
    }

    /**
     * Estime la quantité vendue à un nouveau prix basé sur l'élasticité.
     * Formule: Q2 = Q1 × (P2/P1)^élasticité
     */
    public Integer estimateQuantityAtPrice(Integer quantiteReference, Double prixReference, 
                                            Double nouveauPrix, Double elasticite) {
        if (quantiteReference == null || prixReference == null || nouveauPrix == null || elasticite == null) {
            return quantiteReference;
        }

        if (prixReference <= 0 || nouveauPrix <= 0) {
            return quantiteReference;
        }

        // Formule: Q2 = Q1 × (P2/P1)^β où β = élasticité
        double ratioPrix = nouveauPrix / prixReference;
        double multiplicateur = Math.pow(ratioPrix, elasticite);
        
        int quantiteEstimee = (int) Math.round(quantiteReference * multiplicateur);
        
        log.debug("Estimation quantité: {} à prix {} (réf: {} @ {}), multiplicateur: {}",
                quantiteEstimee, nouveauPrix, quantiteReference, prixReference, 
                String.format("%.3f", multiplicateur));

        return Math.max(0, quantiteEstimee);
    }

    /**
     * Calcule le revenu estimé à un prix donné.
     */
    public Double estimateRevenue(Integer quantiteEstimee, Double prix) {
        if (quantiteEstimee == null || prix == null) return 0.0;
        return quantiteEstimee * prix;
    }

    /**
     * Détermine la stratégie pricing optimale selon les caractéristiques du produit.
     */
    public PricingStrategyRecommendation determineOptimalStrategy(Double elasticite, Double rotationStock) {
        if (elasticite == null) {
            return new PricingStrategyRecommendation(PricingConfiguration.StrategiePricing.DYNAMIQUE, 
                    "Stratégie dynamique par défaut");
        }

        // Produit à forte rotation + élastique → Pénétration (prix bas, volume haut)
        if (elasticite < -1.5 && rotationStock != null && rotationStock > 0.7) {
            return new PricingStrategyRecommendation(PricingConfiguration.StrategiePricing.PENETRATION,
                    "Produit élastique à forte rotation: stratégie pénétration pour maximiser volume");
        }

        // Produit inélastique + faible rotation → Skimming (prix haut, marge max)
        if (elasticite > -0.8 && (rotationStock == null || rotationStock < 0.3)) {
            return new PricingStrategyRecommendation(PricingConfiguration.StrategiePricing.SKIMMING,
                    "Produit inélastique à faible rotation: stratégie skimming pour maximiser marge");
        }

        // Produit concurrentiel → Suivi de prix
        if (Math.abs(elasticite + 1.0) < 0.3) {
            return new PricingStrategyRecommendation(PricingConfiguration.StrategiePricing.COMPETITIVE,
                    "Élasticité proche de -1: marché concurrentiel, suivi des prix recommandé");
        }

        // Cas par défaut
        return new PricingStrategyRecommendation(PricingConfiguration.StrategiePricing.ELASTICITE_BASEE,
                "Utilisation de l'élasticité calculée pour ajustements dynamiques");
    }

    /**
     * Classe interne pour recommandation de stratégie.
     */
    public record PricingStrategyRecommendation(
            PricingConfiguration.StrategiePricing strategie,
            String justification
    ) {}
}
