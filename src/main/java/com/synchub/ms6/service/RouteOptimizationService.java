package com.synchub.ms6.service;

import com.synchub.ms6.entity.PointLivraison;
import com.synchub.ms6.entity.RouteLivraison;
import com.synchub.ms6.repository.PointLivraisonRepository;
import com.synchub.ms6.repository.RouteLivraisonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service d'optimisation des routes de livraison.
 * Implémente les algorithmes de Clark-Wright (Savings Algorithm) et 2-Opt pour résoudre
 * le problème de tournées de véhicules (VRP - Vehicle Routing Problem).
 *
 * Complexité algorithmique:
 * - Clark-Wright: O(n² log n)
 * - 2-Opt: O(k × n²) où k est le nombre d'itérations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {

    private final RouteLivraisonRepository routeRepository;
    private final PointLivraisonRepository pointRepository;

    // Vitesse moyenne estimée en km/h pour les calculs de temps
    private static final double VITESSE_MOYENNE_KMH = 40.0;
    // Temps de service moyen par point en minutes
    private static final int TEMPS_SERVICE_MOYEN_MINUTES = 10;
    // Rayon de la Terre pour calcul Haversine (km)
    private static final double RAYON_TERRE_KM = 6371.0;

    /**
     * Algorithme de Clarke-Wright (Savings Algorithm)
     * Heuristique constructive pour le VRP qui fusionne les routes
     * en maximisant les "savings" (économies de distance).
     *
     * Formule du savings: s(i,j) = d(0,i) + d(0,j) - d(i,j)
     * où 0 est le dépôt, i et j sont des clients
     */
    @Transactional
    public RouteLivraison optimizeRouteWithClarkeWright(
            String routeName,
            List<DeliveryPointRequest> points,
            DepotRequest depot,
            VehicleConstraints constraints) {

        log.info("Démarrage de l'optimisation Clarke-Wright pour {} points", points.size());

        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Au moins 2 points de livraison sont requis");
        }

        // Étape 1: Calculer la matrice de distances
        double[][] distanceMatrix = calculateDistanceMatrix(points, depot);

        // Étape 2: Calculer les savings pour toutes les paires
        List<Saving> savings = calculateSavings(points.size(), distanceMatrix);

        // Étape 3: Trier les savings par ordre décroissant
        savings.sort((a, b) -> Double.compare(b.savingValue, a.savingValue));

        // Étape 4: Construire les routes initiales (chaque client = une route)
        List<Route> routes = initializeRoutes(points);

        // Étape 5: Fusionner les routes selon les savings
        int iterations = mergeRoutesBySavings(routes, savings, distanceMatrix, constraints);

        // Étape 6: Sélectionner la meilleure route (ou combiner si une seule route souhaitée)
        Route bestRoute = selectBestRoute(routes, distanceMatrix);

        // Étape 7: Appliquer l'optimisation 2-Opt pour améliorer localement
        double distanceBefore2Opt = calculateTotalDistance(bestRoute, distanceMatrix);
        int swaps = applyTwoOpt(bestRoute, distanceMatrix);
        double distanceAfter2Opt = calculateTotalDistance(bestRoute, distanceMatrix);
        double improvementPercent = ((distanceBefore2Opt - distanceAfter2Opt) / distanceBefore2Opt) * 100;

        log.info("Optimisation 2-Opt: {} swaps, amélioration de {}%", swaps, String.format("%.2f", improvementPercent));

        // Étape 8: Créer et sauvegarder la route optimisée
        RouteLivraison optimizedRoute = buildAndSaveOptimizedRoute(
                routeName, bestRoute, points, depot, distanceMatrix,
                iterations, swaps, improvementPercent, constraints);

        log.info("Route optimisée créée: ID={}, Distance={}km, Points={}",
                optimizedRoute.getRouteId(),
                String.format("%.2f", optimizedRoute.getDistanceTotaleKm()),
                optimizedRoute.getNbPoints());

        return optimizedRoute;
    }

    /**
     * Algorithme 2-Opt pour l'amélioration locale d'une route.
     * Cherche à éliminer les croisements dans la route en inversant des segments.
     *
     * Principe: Si les arêtes (i,i+1) et (j,j+1) se croisent,
     * les remplacer par (i,j) et (i+1,j+1) réduit la distance totale.
     */
    public int applyTwoOpt(Route route, double[][] distanceMatrix) {
        int n = route.pointIndices.size();
        if (n < 4) return 0;

        boolean improved = true;
        int iterations = 0;
        int totalSwaps = 0;

        while (improved && iterations < 100) {
            improved = false;
            iterations++;

            for (int i = 0; i < n - 2; i++) {
                for (int j = i + 2; j < n - 1; j++) {
                    // Calculer le gain potentiel du swap 2-opt
                    int iNext = route.pointIndices.get(i + 1);
                    int jNext = route.pointIndices.get(j + 1);
                    int iCurrent = route.pointIndices.get(i);
                    int jCurrent = route.pointIndices.get(j);

                    double currentDist = distanceMatrix[iCurrent][iNext] + distanceMatrix[jCurrent][jNext];
                    double newDist = distanceMatrix[iCurrent][jCurrent] + distanceMatrix[iNext][jNext];
                    double gain = currentDist - newDist;

                    if (gain > 0.001) {
                        // Effectuer le swap: inverser le segment entre i+1 et j
                        reverseSegment(route.pointIndices, i + 1, j);
                        route.totalDistance -= gain;
                        improved = true;
                        totalSwaps++;
                    }
                }
            }
        }

        return totalSwaps;
    }

    /**
     * Calcul de la matrice des distances entre tous les points.
     * Inclut le dépôt à l'index 0.
     */
    private double[][] calculateDistanceMatrix(List<DeliveryPointRequest> points, DepotRequest depot) {
        int n = points.size() + 1; // +1 pour le dépôt
        double[][] matrix = new double[n][n];

        // Coordonnées: index 0 = dépôt, index 1..n = points
        double[] lats = new double[n];
        double[] lngs = new double[n];

        lats[0] = depot.latitude();
        lngs[0] = depot.longitude();

        for (int i = 0; i < points.size(); i++) {
            lats[i + 1] = points.get(i).latitude();
            lngs[i + 1] = points.get(i).longitude();
        }

        // Calculer toutes les distances
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = haversineDistance(lats[i], lngs[i], lats[j], lngs[j]);
                }
            }
        }

        return matrix;
    }

    /**
     * Formule de Haversine pour calculer la distance à vol d'oiseau entre deux coordonnées GPS.
     */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAYON_TERRE_KM * c;
    }

    /**
     * Calcule les "savings" (économies) pour toutes les paires de points.
     * s(i,j) = d(0,i) + d(0,j) - d(i,j)
     */
    private List<Saving> calculateSavings(int numPoints, double[][] distanceMatrix) {
        List<Saving> savings = new ArrayList<>();

        for (int i = 1; i <= numPoints; i++) {
            for (int j = i + 1; j <= numPoints; j++) {
                // Le dépôt est à l'index 0
                double saving = distanceMatrix[0][i] + distanceMatrix[0][j] - distanceMatrix[i][j];
                savings.add(new Saving(i, j, saving));
            }
        }

        return savings;
    }

    /**
     * Initialise les routes: chaque point est une route individuelle.
     */
    private List<Route> initializeRoutes(List<DeliveryPointRequest> points) {
        List<Route> routes = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            Route route = new Route();
            route.pointIndices = new ArrayList<>();
            route.pointIndices.add(i + 1); // +1 car index 0 = dépôt
            route.totalDemand = points.get(i).demand();
            route.totalDistance = 0.0;
            route.isValid = true;
            routes.add(route);
        }

        return routes;
    }

    /**
     * Fusionne les routes en fonction des savings, en respectant les contraintes.
     */
    private int mergeRoutesBySavings(List<Route> routes, List<Saving> savings,
                                      double[][] distanceMatrix, VehicleConstraints constraints) {
        int merges = 0;

        for (Saving saving : savings) {
            // Trouver les routes contenant i et j
            Route routeI = findRouteContaining(routes, saving.i);
            Route routeJ = findRouteContaining(routes, saving.j);

            if (routeI == null || routeJ == null || routeI == routeJ) {
                continue;
            }

            // Vérifier si les points sont aux extrémités de leurs routes
            if (isAtEnd(routeI, saving.i) && isAtEnd(routeJ, saving.j)) {
                // Vérifier les contraintes de capacité
                double combinedDemand = routeI.totalDemand + routeJ.totalDemand;
                if (constraints != null && combinedDemand > constraints.maxCapacity()) {
                    continue;
                }

                // Fusionner les routes
                mergeTwoRoutes(routeI, routeJ, saving.i, saving.j);
                routeI.totalDemand = combinedDemand;
                routeJ.isValid = false; // Marquer comme fusionnée
                merges++;
            }
        }

        // Nettoyer les routes invalides
        routes.removeIf(r -> !r.isValid);

        return merges;
    }

    private Route findRouteContaining(List<Route> routes, int pointIndex) {
        for (Route route : routes) {
            if (route.isValid && route.pointIndices.contains(pointIndex)) {
                return route;
            }
        }
        return null;
    }

    private boolean isAtEnd(Route route, int pointIndex) {
        if (route.pointIndices.isEmpty()) return false;
        return route.pointIndices.get(0) == pointIndex ||
               route.pointIndices.get(route.pointIndices.size() - 1) == pointIndex;
    }

    private void mergeTwoRoutes(Route route1, Route route2, int pointI, int pointJ) {
        // Déterminer l'ordre de fusion
        boolean iAtStart = route1.pointIndices.get(0) == pointI;
        boolean jAtStart = route2.pointIndices.get(0) == pointJ;

        List<Integer> newOrder = new ArrayList<>();

        if (iAtStart && jAtStart) {
            // Inverser route2 et concaténer après route1
            newOrder.addAll(route1.pointIndices);
            List<Integer> reversedRoute2 = new ArrayList<>(route2.pointIndices);
            Collections.reverse(reversedRoute2);
            newOrder.addAll(reversedRoute2);
        } else if (iAtStart && !jAtStart) {
            // route2 normal après route1
            newOrder.addAll(route1.pointIndices);
            newOrder.addAll(route2.pointIndices);
        } else if (!iAtStart && jAtStart) {
            // route2 normal avant route1
            newOrder.addAll(route2.pointIndices);
            newOrder.addAll(route1.pointIndices);
        } else {
            // Inverser route2 et concaténer avant route1
            List<Integer> reversedRoute2 = new ArrayList<>(route2.pointIndices);
            Collections.reverse(reversedRoute2);
            newOrder.addAll(reversedRoute2);
            newOrder.addAll(route1.pointIndices);
        }

        route1.pointIndices = newOrder;
    }

    private void reverseSegment(List<Integer> list, int start, int end) {
        while (start < end) {
            int temp = list.get(start);
            list.set(start, list.get(end));
            list.set(end, temp);
            start++;
            end--;
        }
    }

    private Route selectBestRoute(List<Route> routes, double[][] distanceMatrix) {
        // Retourner la route avec le plus de points ou la plus courte si une seule
        if (routes.size() == 1) {
            Route route = routes.get(0);
            route.totalDistance = calculateTotalDistance(route, distanceMatrix);
            return route;
        }

        // Sinon, combiner toutes les routes en une seule tournée
        Route combined = new Route();
        combined.pointIndices = new ArrayList<>();
        combined.totalDemand = 0.0;

        for (Route route : routes) {
            combined.pointIndices.addAll(route.pointIndices);
            combined.totalDemand += route.totalDemand;
        }

        combined.totalDistance = calculateTotalDistance(combined, distanceMatrix);
        return combined;
    }

    private double calculateTotalDistance(Route route, double[][] distanceMatrix) {
        double total = 0.0;
        int n = route.pointIndices.size();

        // Départ du dépôt
        total += distanceMatrix[0][route.pointIndices.get(0)];

        // Entre les points
        for (int i = 0; i < n - 1; i++) {
            total += distanceMatrix[route.pointIndices.get(i)][route.pointIndices.get(i + 1)];
        }

        // Retour au dépôt
        total += distanceMatrix[route.pointIndices.get(n - 1)][0];

        return total;
    }

    private RouteLivraison buildAndSaveOptimizedRoute(
            String routeName,
            Route optimizedRoute,
            List<DeliveryPointRequest> pointRequests,
            DepotRequest depot,
            double[][] distanceMatrix,
            int clarkWrightIterations,
            int twoOptSwaps,
            double improvementPercent,
            VehicleConstraints constraints) {

        // Créer la route
        RouteLivraison route = RouteLivraison.builder()
                .nom(routeName)
                .pointDepart(depot.adresse())
                .latitudeDepart(depot.latitude())
                .longitudeDepart(depot.longitude())
                .dateOptimisation(LocalDateTime.now())
                .statut(RouteLivraison.StatutRoute.PLANIFIEE)
                .distanceTotaleKm(optimizedRoute.totalDistance)
                .capaciteVehicule(constraints != null ? (int) constraints.maxCapacity() : null)
                .algorithmeUtilise("Clarke-Wright + 2-Opt")
                .iterationOptimisation(clarkWrightIterations)
                .amelioration2OptPercent(improvementPercent)
                .build();

        // Calculer la durée estimée
        int dureeMinutes = (int) ((optimizedRoute.totalDistance / VITESSE_MOYENNE_KMH) * 60)
                + (optimizedRoute.pointIndices.size() * TEMPS_SERVICE_MOYEN_MINUTES);
        route.setDureeEstimeeMinutes(dureeMinutes);

        // Sauvegarder la route
        route = routeRepository.save(route);

        // Créer les points de livraison
        List<PointLivraison> points = new ArrayList<>();
        double distanceCumulee = 0.0;
        int tempsCumule = 0;

        int prevIndex = 0; // Départ du dépôt

        for (int ordre = 0; ordre < optimizedRoute.pointIndices.size(); ordre++) {
            int pointIndex = optimizedRoute.pointIndices.get(ordre) - 1; // -1 car points commencent à 1
            DeliveryPointRequest request = pointRequests.get(pointIndex);

            // Calculer les distances
            double distFromPrev = distanceMatrix[prevIndex][pointIndex + 1];
            double distFromDepot = distanceMatrix[0][pointIndex + 1];
            distanceCumulee += distFromPrev;

            // Calculer le temps
            int tempsTrajet = (int) ((distFromPrev / VITESSE_MOYENNE_KMH) * 60);
            tempsCumule += tempsTrajet + TEMPS_SERVICE_MOYEN_MINUTES;

            PointLivraison point = PointLivraison.builder()
                    .route(route)
                    .ordre(ordre + 1)
                    .adresse(request.adresse())
                    .ville(request.ville())
                    .codePostal(request.codePostal())
                    .latitude(request.latitude())
                    .longitude(request.longitude())
                    .nbColis(request.nbColis())
                    .poidsKg(request.poidsKg())
                    .volumeM3(request.volumeM3())
                    .priorite(request.priorite())
                    .fenetreDebut(request.fenetreDebut())
                    .fenetreFin(request.fenetreFin())
                    .tempsServiceMinutes(request.tempsServiceMinutes() != null ?
                            request.tempsServiceMinutes() : TEMPS_SERVICE_MOYEN_MINUTES)
                    .distancePrecedentKm(distFromPrev)
                    .distanceDepartKm(distFromDepot)
                    .distanceCumuleeKm(distanceCumulee)
                    .tempsEstimeMinutes(tempsTrajet)
                    .tempsCumuleMinutes(tempsCumule)
                    .heureArriveeEstimee(LocalDateTime.now().plusMinutes(tempsCumule))
                    .statut(PointLivraison.StatutPoint.PLANIFIE)
                    .build();

            points.add(point);
            prevIndex = pointIndex + 1;
        }

        // Sauvegarder tous les points et récupérer les instances avec IDs générés
        List<PointLivraison> savedPoints = pointRepository.saveAll(points);
        pointRepository.flush();

        // Mettre à jour les statistiques de la route
        route.setPoints(savedPoints);
        route.calculerStatistiques();

        return routeRepository.save(route);
    }

    // ==================== MÉTHODES PUBLIQUES UTILITAIRES ====================

    @Transactional(readOnly = true)
    public RouteMetrics calculateRouteMetrics(Long routeId) {
        RouteLivraison route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route non trouvée: " + routeId));

        // Charger les points explicitement
        List<PointLivraison> points = pointRepository.findByRouteRouteIdOrderByOrdreAsc(routeId);

        // Calculer les métriques
        double avgDistanceBetweenPoints = points.stream()
                .mapToDouble(PointLivraison::getDistancePrecedentKm)
                .average()
                .orElse(0.0);

        double maxDistanceBetweenPoints = points.stream()
                .mapToDouble(PointLivraison::getDistancePrecedentKm)
                .max()
                .orElse(0.0);

        double efficiency = route.getDistanceTotaleKm() > 0
                ? route.getNbPoints() / route.getDistanceTotaleKm()
                : 0.0;

        long pointsRespectingTimeWindow = points.stream()
                .filter(PointLivraison::respecteFenetreHoraire)
                .count();

        return new RouteMetrics(
                route.getDistanceTotaleKm(),
                route.getDureeEstimeeMinutes(),
                route.getNbPoints(),
                avgDistanceBetweenPoints,
                maxDistanceBetweenPoints,
                efficiency,
                (double) pointsRespectingTimeWindow / points.size() * 100,
                route.getAmelioration2OptPercent()
        );
    }

    @Transactional
    public RouteLivraison reoptimizeRoute(Long routeId) {
        RouteLivraison existingRoute = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route non trouvée: " + routeId));

        // Charger les points explicitement
        existingRoute.setPoints(pointRepository.findByRouteRouteIdOrderByOrdreAsc(routeId));

        if (existingRoute.getStatut() != RouteLivraison.StatutRoute.PLANIFIEE) {
            throw new IllegalStateException("Seules les routes planifiées peuvent être réoptimisées");
        }

        // Sauvegarder les données avant suppression
        String routeName = existingRoute.getNom();
        String pointDepart = existingRoute.getPointDepart();
        Double latDepart = existingRoute.getLatitudeDepart();
        Double lngDepart = existingRoute.getLongitudeDepart();
        Integer capacite = existingRoute.getCapaciteVehicule();

        // Récupérer les points avant suppression
        List<PointLivraison> existingPoints = new ArrayList<>(existingRoute.getPoints());

        // Convertir les points existants en requêtes
        List<DeliveryPointRequest> pointRequests = existingPoints.stream()
                .map(p -> new DeliveryPointRequest(
                        p.getAdresse(),
                        p.getVille(),
                        p.getCodePostal(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getNbColis() != null ? p.getNbColis().doubleValue() : 1.0,
                        p.getNbColis(),
                        p.getPoidsKg(),
                        p.getVolumeM3(),
                        p.getPriorite(),
                        p.getFenetreDebut(),
                        p.getFenetreFin(),
                        p.getTempsServiceMinutes()
                ))
                .toList();

        DepotRequest depot = new DepotRequest(pointDepart, latDepart, lngDepart);

        VehicleConstraints constraints = capacite != null
                ? new VehicleConstraints(capacite.doubleValue())
                : null;

        // Supprimer l'ancienne route et ses points
        pointRepository.deleteAll(existingPoints);
        pointRepository.flush();
        routeRepository.delete(existingRoute);
        routeRepository.flush();

        // Recréer avec optimisation
        return optimizeRouteWithClarkeWright(
                routeName + " (Réoptimisée)",
                pointRequests,
                depot,
                constraints
        );
    }

    // ==================== CLASSES INTERNES ET RECORDS ====================

    private static class Route {
        List<Integer> pointIndices;
        double totalDemand;
        double totalDistance;
        boolean isValid;
    }

    private static class Saving {
        final int i;
        final int j;
        final double savingValue;

        Saving(int i, int j, double savingValue) {
            this.i = i;
            this.j = j;
            this.savingValue = savingValue;
        }
    }

    public record DeliveryPointRequest(
            String adresse,
            String ville,
            String codePostal,
            double latitude,
            double longitude,
            double demand,
            Integer nbColis,
            Double poidsKg,
            Double volumeM3,
            Integer priorite,
            LocalDateTime fenetreDebut,
            LocalDateTime fenetreFin,
            Integer tempsServiceMinutes
    ) {}

    public record DepotRequest(
            String adresse,
            double latitude,
            double longitude
    ) {}

    public record VehicleConstraints(
            double maxCapacity
    ) {}

    public record RouteMetrics(
            double totalDistanceKm,
            int estimatedDurationMinutes,
            int nbPoints,
            double avgDistanceBetweenPoints,
            double maxDistanceBetweenPoints,
            double efficiencyPointsPerKm,
            double timeWindowRespectPercent,
            Double twoOptImprovementPercent
    ) {}
}
