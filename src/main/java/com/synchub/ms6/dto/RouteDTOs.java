package com.synchub.ms6.dto;

import com.synchub.ms6.entity.PointLivraison;
import com.synchub.ms6.entity.RouteLivraison;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs pour l'optimisation des routes de livraison
 */
public class RouteDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizeRouteRequest {
        private String routeName;
        private DepotDTO depot;
        private List<DeliveryPointDTO> points;
        private VehicleConstraintsDTO constraints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepotDTO {
        private String adresse;
        private double latitude;
        private double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryPointDTO {
        private Long livraisonId;
        private Long commandeId;
        private String adresse;
        private String ville;
        private String codePostal;
        private double latitude;
        private double longitude;
        private double demand;
        private Integer nbColis;
        private Double poidsKg;
        private Double volumeM3;
        private Integer priorite;
        private LocalDateTime fenetreDebut;
        private LocalDateTime fenetreFin;
        private Integer tempsServiceMinutes;
        private String nomContact;
        private String telephoneContact;
        private String instructionsSpeciales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleConstraintsDTO {
        private double maxCapacity;
        private Double maxWeightKg;
        private Double maxVolumeM3;
        private Integer maxDurationMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteResponse {
        private Long routeId;
        private String nom;
        private RouteLivraison.StatutRoute statut;
        private LocalDateTime dateCreation;
        private LocalDateTime dateOptimisation;
        private Double distanceTotaleKm;
        private Integer dureeEstimeeMinutes;
        private Double coutEstime;
        private Integer nbPoints;
        private Integer nbColis;
        private Double poidsTotalKg;
        private Double volumeTotalM3;
        private String pointDepart;
        private Double latitudeDepart;
        private Double longitudeDepart;
        private String transporteur;
        private String algorithmeUtilise;
        private Integer iterationOptimisation;
        private Double amelioration2OptPercent;
        private List<PointLivraisonDTO> points;
        private RouteMetricsDTO metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointLivraisonDTO {
        private Long pointId;
        private Long livraisonId;
        private Long commandeId;
        private String adresse;
        private String ville;
        private String codePostal;
        private Double latitude;
        private Double longitude;
        private Integer ordre;
        private Double distancePrecedentKm;
        private Double distanceDepartKm;
        private Double distanceCumuleeKm;
        private Integer tempsEstimeMinutes;
        private Integer tempsCumuleMinutes;
        private LocalDateTime heureArriveeEstimee;
        private LocalDateTime fenetreDebut;
        private LocalDateTime fenetreFin;
        private Integer nbColis;
        private Double poidsKg;
        private Double volumeM3;
        private PointLivraison.StatutPoint statut;
        private Integer priorite;
        private String nomContact;
        private String telephoneContact;
        private String instructionsSpeciales;
        private Double penaliteFenetreHoraire;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteMetricsDTO {
        private Double totalDistanceKm;
        private Integer estimatedDurationMinutes;
        private Integer nbPoints;
        private Double avgDistanceBetweenPoints;
        private Double maxDistanceBetweenPoints;
        private Double efficiencyPointsPerKm;
        private Double timeWindowRespectPercent;
        private Double twoOptImprovementPercent;
        private Double densitePoints;
        private Boolean respecteContraintesCapacite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteOptimizationResult {
        private RouteResponse route;
        private OptimizationStatsDTO optimizationStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationStatsDTO {
        private String algorithmePrincipal;
        private String algorithmeSecondaire;
        private Integer nbPointsOptimises;
        private Integer iterationsClarkeWright;
        private Integer swaps2Opt;
        private Double distanceInitialeEstimee;
        private Double distanceFinale;
        private Double pourcentageAmelioration;
        private Long tempsCalculMs;
        private String complexiteAlgorithmique;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSummaryDTO {
        private Long routeId;
        private String nom;
        private RouteLivraison.StatutRoute statut;
        private LocalDateTime dateOptimisation;
        private Double distanceTotaleKm;
        private Integer nbPoints;
        private String transporteur;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteListResponse {
        private List<RouteSummaryDTO> routes;
        private Integer totalCount;
        private Integer activeCount;
        private Integer completedCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRouteStatusRequest {
        private String statut;  // String pour éviter problème de deserialization
        private String transporteur;
        private LocalDateTime fenetreDebut;
        private LocalDateTime fenetreFin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteComparisonDTO {
        private Long routeId1;
        private Long routeId2;
        private Double distanceDifference;
        private Double durationDifference;
        private Double efficiencyComparison;
        private String recommendation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOptimizeRequest {
        private List<OptimizeRouteRequest> routes;
        private Boolean parallelProcessing;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOptimizeResponse {
        private List<RouteOptimizationResult> results;
        private Integer successCount;
        private Integer failureCount;
        private Long totalProcessingTimeMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteAnalyticsDTO {
        private Double averageDistancePerRoute;
        private Double averagePointsPerRoute;
        private Double averageEfficiency;
        private Long totalRoutes;
        private Long totalDistanceKm;
        private Long totalPointsDelivered;
        private List<TransporteurStatsDTO> statsByTransporteur;
        private List<DailyRouteStatsDTO> dailyStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransporteurStatsDTO {
        private String transporteur;
        private Long nbRoutes;
        private Double totalDistance;
        private Double averageDistance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRouteStatsDTO {
        private String date;
        private Long nbRoutes;
        private Double totalDistance;
        private Integer totalPoints;
    }
}
