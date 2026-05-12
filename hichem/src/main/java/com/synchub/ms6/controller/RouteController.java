package com.synchub.ms6.controller;

import com.synchub.ms6.dto.RouteDTOs;
import com.synchub.ms6.dto.RouteDTOs.*;
import com.synchub.ms6.entity.PointLivraison;
import com.synchub.ms6.entity.RouteLivraison;
import com.synchub.ms6.repository.PointLivraisonRepository;
import com.synchub.ms6.repository.RouteLivraisonRepository;
import com.synchub.ms6.service.RouteOptimizationService;
import com.synchub.ms6.service.RouteOptimizationService.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des routes de livraison optimisées.
 * Expose les algorithmes de Clarke-Wright (Savings) et 2-Opt pour le VRP.
 */
@RestController
@RequestMapping("/ms6/api/routes")
@RequiredArgsConstructor
@Slf4j
public class RouteController {

    private final RouteOptimizationService optimizationService;
    private final RouteLivraisonRepository routeRepository;
    private final PointLivraisonRepository pointRepository;

    /**
     * Optimise une nouvelle route avec l'algorithme de Clarke-Wright + 2-Opt
     */
    @PostMapping("/optimize")
    public ResponseEntity<RouteOptimizationResult> optimizeRoute(
            @RequestBody OptimizeRouteRequest request) {

        log.info("Requête d'optimisation de route: {} avec {} points",
                request.getRouteName(), request.getPoints().size());

        long startTime = System.currentTimeMillis();

        // Convertir les DTOs en paramètres du service
        List<DeliveryPointRequest> pointRequests = request.getPoints().stream()
                .map(this::convertToDeliveryPointRequest)
                .toList();

        DepotRequest depot = new DepotRequest(
                request.getDepot().getAdresse(),
                request.getDepot().getLatitude(),
                request.getDepot().getLongitude()
        );

        VehicleConstraints constraints = request.getConstraints() != null
                ? new VehicleConstraints(request.getConstraints().getMaxCapacity())
                : null;

        // Exécuter l'optimisation
        RouteLivraison optimizedRoute = optimizationService.optimizeRouteWithClarkeWright(
                request.getRouteName(),
                pointRequests,
                depot,
                constraints
        );

        long processingTime = System.currentTimeMillis() - startTime;

        // Construire la réponse
        RouteResponse routeResponse = convertToRouteResponse(optimizedRoute);
        OptimizationStatsDTO stats = OptimizationStatsDTO.builder()
                .algorithmePrincipal("Clarke-Wright (Savings)")
                .algorithmeSecondaire("2-Opt Local Search")
                .nbPointsOptimises(request.getPoints().size())
                .iterationsClarkeWright(optimizedRoute.getIterationOptimisation())
                .swaps2Opt(0)
                .distanceInitialeEstimee(optimizedRoute.getDistanceTotaleKm() * 1.15)
                .distanceFinale(optimizedRoute.getDistanceTotaleKm())
                .pourcentageAmelioration(optimizedRoute.getAmelioration2OptPercent())
                .tempsCalculMs(processingTime)
                .complexiteAlgorithmique("O(n² log n) pour Clarke-Wright, O(k×n²) pour 2-Opt")
                .build();

        RouteOptimizationResult result = RouteOptimizationResult.builder()
                .route(routeResponse)
                .optimizationStats(stats)
                .build();

        log.info("Route optimisée créée avec succès: ID={}, Temps={}ms",
                optimizedRoute.getRouteId(), processingTime);

        return ResponseEntity.ok(result);
    }

    /**
     * Récupère toutes les routes
     */
    @GetMapping
    public ResponseEntity<RouteListResponse> getAllRoutes(
            @RequestParam(required = false) RouteLivraison.StatutRoute statut) {

        List<RouteLivraison> routes;
        if (statut != null) {
            routes = routeRepository.findByStatut(statut);
        } else {
            routes = routeRepository.findAll();
        }

        List<RouteSummaryDTO> summaries = routes.stream()
                .map(this::convertToRouteSummary)
                .toList();

        long activeCount = routes.stream()
                .filter(r -> r.getStatut() == RouteLivraison.StatutRoute.EN_COURS)
                .count();
        long completedCount = routes.stream()
                .filter(r -> r.getStatut() == RouteLivraison.StatutRoute.TERMINEE)
                .count();

        RouteListResponse response = RouteListResponse.builder()
                .routes(summaries)
                .totalCount(routes.size())
                .activeCount((int) activeCount)
                .completedCount((int) completedCount)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère une route par ID avec tous ses points
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        RouteLivraison route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route non trouvée: " + id));

        // Charger explicitement les points
        route.getPoints().size(); // Force le chargement lazy

        RouteResponse response = convertToRouteResponse(route);

        // Ajouter les métriques
        RouteMetrics metrics = optimizationService.calculateRouteMetrics(id);
        response.setMetrics(convertToMetricsDTO(metrics, route));

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les métriques détaillées d'une route
     */
    @GetMapping("/{id}/metrics")
    public ResponseEntity<RouteMetricsDTO> getRouteMetrics(@PathVariable Long id) {
        RouteMetrics metrics = optimizationService.calculateRouteMetrics(id);
        RouteLivraison route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route non trouvée: " + id));

        return ResponseEntity.ok(convertToMetricsDTO(metrics, route));
    }

    /**
     * Met à jour le statut d'une route
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<RouteResponse> updateRouteStatus(
            @PathVariable Long id,
            @RequestBody UpdateRouteStatusRequest request) {

        RouteLivraison route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route non trouvée: " + id));

        // Charger explicitement les points
        route.getPoints().size();

        if (request.getStatut() != null) {
            route.setStatut(RouteLivraison.StatutRoute.valueOf(request.getStatut()));
        }
        if (request.getTransporteur() != null) {
            route.setTransporteur(request.getTransporteur());
        }
        if (request.getFenetreDebut() != null) {
            route.setFenetreDebut(request.getFenetreDebut());
        }
        if (request.getFenetreFin() != null) {
            route.setFenetreFin(request.getFenetreFin());
        }

        RouteLivraison saved = routeRepository.save(route);
        return ResponseEntity.ok(convertToRouteResponse(saved));
    }

    /**
     * Réoptimise une route existante
     */
    @PostMapping("/{id}/reoptimize")
    public ResponseEntity<RouteOptimizationResult> reoptimizeRoute(@PathVariable Long id) {
        log.info("Réoptimisation de la route: {}", id);

        long startTime = System.currentTimeMillis();

        RouteLivraison reoptimized = optimizationService.reoptimizeRoute(id);

        long processingTime = System.currentTimeMillis() - startTime;

        RouteResponse routeResponse = convertToRouteResponse(reoptimized);
        OptimizationStatsDTO stats = OptimizationStatsDTO.builder()
                .algorithmePrincipal("Clarke-Wright (Savings)")
                .algorithmeSecondaire("2-Opt Local Search")
                .nbPointsOptimises(reoptimized.getNbPoints())
                .iterationsClarkeWright(reoptimized.getIterationOptimisation())
                .swaps2Opt(0)
                .distanceFinale(reoptimized.getDistanceTotaleKm())
                .pourcentageAmelioration(reoptimized.getAmelioration2OptPercent())
                .tempsCalculMs(processingTime)
                .complexiteAlgorithmique("O(n² log n) pour Clarke-Wright")
                .build();

        RouteOptimizationResult result = RouteOptimizationResult.builder()
                .route(routeResponse)
                .optimizationStats(stats)
                .build();

        return ResponseEntity.ok(result);
    }

    /**
     * Supprime une route
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        RouteLivraison route = routeRepository.findByIdWithPoints(id)
                .orElseThrow(() -> new EntityNotFoundException("Route non trouvée: " + id));

        // Supprimer d'abord les points
        pointRepository.deleteAll(route.getPoints());
        // Puis la route
        routeRepository.delete(route);

        log.info("Route supprimée: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les routes par transporteur
     */
    @GetMapping("/by-transporteur/{transporteur}")
    public ResponseEntity<List<RouteSummaryDTO>> getRoutesByTransporteur(
            @PathVariable String transporteur) {

        List<RouteLivraison> routes = routeRepository.findByTransporteur(transporteur);

        List<RouteSummaryDTO> summaries = routes.stream()
                .map(this::convertToRouteSummary)
                .toList();

        return ResponseEntity.ok(summaries);
    }

    /**
     * Récupère les statistiques analytiques des routes
     */
    @GetMapping("/analytics")
    public ResponseEntity<RouteAnalyticsDTO> getRouteAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<RouteLivraison> routes = routeRepository.findByDateCreationBetween(debut, fin);

        double avgDistance = routes.stream()
                .mapToDouble(RouteLivraison::getDistanceTotaleKm)
                .average()
                .orElse(0.0);

        double avgPoints = routes.stream()
                .mapToInt(RouteLivraison::getNbPoints)
                .average()
                .orElse(0.0);

        double totalDistance = routes.stream()
                .mapToDouble(RouteLivraison::getDistanceTotaleKm)
                .sum();

        int totalPoints = routes.stream()
                .mapToInt(RouteLivraison::getNbPoints)
                .sum();

        RouteAnalyticsDTO analytics = RouteAnalyticsDTO.builder()
                .averageDistancePerRoute(avgDistance)
                .averagePointsPerRoute(avgPoints)
                .averageEfficiency(routes.stream()
                        .mapToDouble(RouteLivraison::calculerDensite)
                        .average()
                        .orElse(0.0))
                .totalRoutes((long) routes.size())
                .totalDistanceKm((long) totalDistance)
                .totalPointsDelivered((long) totalPoints)
                .statsByTransporteur(List.of())
                .dailyStats(List.of())
                .build();

        return ResponseEntity.ok(analytics);
    }

    /**
     * Met à jour le statut d'un point de livraison
     */
    @PutMapping("/points/{pointId}/status")
    public ResponseEntity<PointLivraisonDTO> updatePointStatus(
            @PathVariable Long pointId,
            @RequestParam String statut) {

        PointLivraison point = pointRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException("Point non trouvé: " + pointId));

        try {
            point.setStatut(PointLivraison.StatutPoint.valueOf(statut));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide: " + statut + ". Valeurs acceptées: PLANIFIE, EN_ROUTE, ARRIVE, LIVRE, NON_LIVRE");
        }
        PointLivraison saved = pointRepository.save(point);

        return ResponseEntity.ok(convertToPointDTO(saved));
    }

    // ==================== MÉTHODES DE CONVERSION ====================

    private DeliveryPointRequest convertToDeliveryPointRequest(DeliveryPointDTO dto) {
        return new DeliveryPointRequest(
                dto.getAdresse(),
                dto.getVille(),
                dto.getCodePostal(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getDemand(),
                dto.getNbColis(),
                dto.getPoidsKg(),
                dto.getVolumeM3(),
                dto.getPriorite(),
                dto.getFenetreDebut(),
                dto.getFenetreFin(),
                dto.getTempsServiceMinutes()
        );
    }

    private RouteResponse convertToRouteResponse(RouteLivraison route) {
        List<PointLivraisonDTO> points = route.getPoints() != null
                ? route.getPoints().stream()
                        .sorted((p1, p2) -> Integer.compare(
                                p1.getOrdre() != null ? p1.getOrdre() : 0,
                                p2.getOrdre() != null ? p2.getOrdre() : 0))
                        .map(this::convertToPointDTO)
                        .collect(Collectors.toList())
                : List.of();

        return RouteResponse.builder()
                .routeId(route.getRouteId())
                .nom(route.getNom())
                .statut(route.getStatut())
                .dateCreation(route.getDateCreation())
                .dateOptimisation(route.getDateOptimisation())
                .distanceTotaleKm(route.getDistanceTotaleKm())
                .dureeEstimeeMinutes(route.getDureeEstimeeMinutes())
                .coutEstime(route.getCoutEstime())
                .nbPoints(route.getNbPoints())
                .nbColis(route.getNbColis())
                .poidsTotalKg(route.getPoidsTotalKg())
                .volumeTotalM3(route.getVolumeTotalM3())
                .pointDepart(route.getPointDepart())
                .latitudeDepart(route.getLatitudeDepart())
                .longitudeDepart(route.getLongitudeDepart())
                .transporteur(route.getTransporteur())
                .algorithmeUtilise(route.getAlgorithmeUtilise())
                .iterationOptimisation(route.getIterationOptimisation())
                .amelioration2OptPercent(route.getAmelioration2OptPercent())
                .points(points)
                .build();
    }

    private PointLivraisonDTO convertToPointDTO(PointLivraison point) {
        return PointLivraisonDTO.builder()
                .pointId(point.getPointId())
                .livraisonId(point.getLivraison() != null ? point.getLivraison().getLivraisonId() : null)
                .commandeId(point.getCommande() != null ? point.getCommande().getCommandeId() : null)
                .adresse(point.getAdresse())
                .ville(point.getVille())
                .codePostal(point.getCodePostal())
                .latitude(point.getLatitude())
                .longitude(point.getLongitude())
                .ordre(point.getOrdre())
                .distancePrecedentKm(point.getDistancePrecedentKm())
                .distanceDepartKm(point.getDistanceDepartKm())
                .distanceCumuleeKm(point.getDistanceCumuleeKm())
                .tempsEstimeMinutes(point.getTempsEstimeMinutes())
                .tempsCumuleMinutes(point.getTempsCumuleMinutes())
                .heureArriveeEstimee(point.getHeureArriveeEstimee())
                .fenetreDebut(point.getFenetreDebut())
                .fenetreFin(point.getFenetreFin())
                .nbColis(point.getNbColis())
                .poidsKg(point.getPoidsKg())
                .volumeM3(point.getVolumeM3())
                .statut(point.getStatut())
                .priorite(point.getPriorite())
                .nomContact(point.getNomContact())
                .telephoneContact(point.getTelephoneContact())
                .instructionsSpeciales(point.getInstructionsSpeciales())
                .penaliteFenetreHoraire(point.calculerPenalite())
                .build();
    }

    private RouteSummaryDTO convertToRouteSummary(RouteLivraison route) {
        return RouteSummaryDTO.builder()
                .routeId(route.getRouteId())
                .nom(route.getNom())
                .statut(route.getStatut())
                .dateOptimisation(route.getDateOptimisation())
                .distanceTotaleKm(route.getDistanceTotaleKm())
                .nbPoints(route.getNbPoints())
                .transporteur(route.getTransporteur())
                .build();
    }

    private RouteMetricsDTO convertToMetricsDTO(RouteMetrics metrics, RouteLivraison route) {
        return RouteMetricsDTO.builder()
                .totalDistanceKm(metrics.totalDistanceKm())
                .estimatedDurationMinutes(metrics.estimatedDurationMinutes())
                .nbPoints(metrics.nbPoints())
                .avgDistanceBetweenPoints(metrics.avgDistanceBetweenPoints())
                .maxDistanceBetweenPoints(metrics.maxDistanceBetweenPoints())
                .efficiencyPointsPerKm(metrics.efficiencyPointsPerKm())
                .timeWindowRespectPercent(metrics.timeWindowRespectPercent())
                .twoOptImprovementPercent(metrics.twoOptImprovementPercent())
                .densitePoints(route.calculerDensite())
                .respecteContraintesCapacite(route.respecteContraintesCapacite())
                .build();
    }
}
