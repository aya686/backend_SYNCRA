package com.synchub.ms6.dto;

import com.synchub.ms6.entity.PricingConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PricingDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingConfigurationRequest {
        private Long produitId;
        private Double prixBase;
        private Double prixMin;
        private Double prixMax;
        private PricingConfiguration.StrategiePricing strategie;
        private Double coefficientDemande;
        private Double coefficientConcurrence;
        private Double coefficientSaisonnalite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingConfigurationResponse {
        private Long configId;
        private Long produitId;
        private String nomProduit;
        private Double prixBase;
        private Double prixMin;
        private Double prixMax;
        private PricingConfiguration.StrategiePricing strategie;
        private Double elasticitePrix;
        private Double coefficientDemande;
        private Double coefficientConcurrence;
        private Double coefficientSaisonnalite;
        private Boolean actif;
        private LocalDateTime dateCreation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceCalculationRequest {
        private Long produitId;
        private Double prixActuel;
        private Integer stockDisponible;
        private Integer vuesDerniereHeure;
        private Integer ventesDerniereHeure;
        private Double prixConcurrent;
        private Boolean appliquerAutomatiquement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceCalculationResponse {
        private Long produitId;
        private String nomProduit;
        private Double prixActuel;
        private Double prixCalcule;
        private Double variationPercent;
        private String raisonAjustement;
        private Double elasticiteEstimee;
        private Double revenuEstimeAncienPrix;
        private Double revenuEstimeNouveauPrix;
        private Double gainRevenuPotentiel;
        private List<String> facteursConsideres;
        private Boolean appliquable;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElasticityCalculationRequest {
        private Long produitId;
        private Integer periodeJours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElasticityCalculationResponse {
        private Long produitId;
        private String nomProduit;
        private Double elasticiteCalculee;
        private String interpretation;
        private List<DataPoint> pointsDonnees;
        private Double coefficientDetermination;
        private String formuleUtilisee;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime date;
        private Double prix;
        private Integer quantite;
        private Double elasticitePonctuelle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSimulationRequest {
        private Long produitId;
        private Double nouveauPrix;
        private Integer stockDisponible;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceSimulationResponse {
        private Long produitId;
        private String nomProduit;
        private Double prixActuel;
        private Double prixSimule;
        private Double variationPercent;
        private Integer quantiteEstimeeAncienPrix;
        private Integer quantiteEstimeeNouveauPrix;
        private Double revenuAncienPrix;
        private Double revenuNouveauPrix;
        private Double margeEstimee;
        private String recommandation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceHistoryDTO {
        private Long historyId;
        private Double prixAvant;
        private Double prixApres;
        private Double variationPercent;
        private String raison;
        private String facteurDeclencheur;
        private Integer quantiteVenduePeriode;
        private Double revenuPeriode;
        private Double impactRevenu;
        private LocalDateTime dateChangement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandDataDTO {
        private Long demandId;
        private LocalDateTime datePeriode;
        private Double prixApplique;
        private Integer quantiteDemandee;
        private Integer quantiteVendue;
        private Integer vuesProduit;
        private Integer paniersAjoutes;
        private Double tauxConversion;
        private Integer stockDisponible;
        private String periodeType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchPriceOptimizationRequest {
        private List<Long> produitIds;
        private Boolean appliquerAutomatiquement;
        private Double seuilVariationMin; // Minimum % pour appliquer changement
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchPriceOptimizationResponse {
        private Integer totalProduits;
        private Integer produitsOptimises;
        private Integer produitsRejetes;
        private List<PriceCalculationResponse> resultats;
        private Double gainRevenuTotalEstime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingAnalyticsDTO {
        private Integer totalProduitsDynamiques;
        private Integer totalChangements24h;
        private Double variationPrixMoyenne;
        private Double gainRevenuTotal;
        private List<TopVariationDTO> topAugmentations;
        private List<TopVariationDTO> topReductions;
        private List<StrategieStatsDTO> repartitionStrategies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopVariationDTO {
        private Long produitId;
        private String nomProduit;
        private Double ancienPrix;
        private Double nouveauPrix;
        private Double variationPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategieStatsDTO {
        private String strategie;
        private Long count;
        private Double pourcentage;
    }
}
