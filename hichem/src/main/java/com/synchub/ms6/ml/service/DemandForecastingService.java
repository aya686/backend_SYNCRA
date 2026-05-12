package com.synchub.ms6.ml.service;

import com.synchub.ms6.entity.LigneCommande;
import com.synchub.ms6.entity.Produit;
import com.synchub.ms6.entity.Stock;
import com.synchub.ms6.ml.model.DemandForecaster;
import com.synchub.ms6.ml.model.DemandForecaster.*;
import com.synchub.ms6.repository.LigneCommandeRepository;
import com.synchub.ms6.repository.ProduitRepository;
import com.synchub.ms6.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service de prévision de la demande
 * Analyse les ventes historiques et prédit la demande future pour chaque produit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemandForecastingService {

    private final DemandForecaster forecaster;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;

    // Cache des prévisions par produit
    private final Map<Long, List<ForecastPoint>> forecastCache = new HashMap<>();
    private final Map<Long, LocalDateTime> cacheTimestamps = new HashMap<>();
    private static final long CACHE_VALIDITY_HOURS = 6;

    /**
     * Entraîne les modèles de prévision pour tous les produits
     * Exécuté tous les jours à 4h du matin
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional(readOnly = true)
    public void retrainAllModels() {
        log.info("Démarrage de l'entraînement des modèles de prévision de demande...");
        
        List<Produit> produits = produitRepository.findByActifTrue();
        int trainedCount = 0;
        
        for (Produit produit : produits) {
            try {
                trainModelForProduct(produit.getProduitId());
                trainedCount++;
            } catch (Exception e) {
                log.warn("Échec de l'entraînement pour le produit {}: {}", 
                         produit.getProduitId(), e.getMessage());
            }
        }
        
        log.info("{} modèles de prévision entraînés avec succès", trainedCount);
        
        // Vider le cache
        forecastCache.clear();
        cacheTimestamps.clear();
    }

    /**
     * Entraîne le modèle pour un produit spécifique
     */
    @Transactional(readOnly = true)
    public void trainModelForProduct(Long produitId) {
        // Récupérer les ventes historiques (90 derniers jours)
        List<TimePoint> salesData = getHistoricalSales(produitId, 90);
        
        if (salesData.size() < 14) {
            log.debug("Pas assez d'historique pour le produit {} ({} jours, min 14)", 
                      produitId, salesData.size());
            return;
        }

        // Entraîner le modèle
        forecaster.train(salesData);
        
        // Générer et mettre en cache les prévisions
        List<ForecastPoint> forecasts = forecaster.forecast(30); // 30 jours
        forecastCache.put(produitId, forecasts);
        cacheTimestamps.put(produitId, LocalDateTime.now());
        
        log.debug("Modèle entraîné pour le produit {} - {} points d'historique", 
                  produitId, salesData.size());
    }

    /**
     * Prédit la demande pour un produit sur les prochains jours
     */
    public List<ForecastPoint> getDemandForecast(Long produitId, int days) {
        // Vérifier le cache
        if (isCacheValid(produitId)) {
            List<ForecastPoint> cached = forecastCache.get(produitId);
            if (cached != null && cached.size() >= days) {
                return cached.subList(0, days);
            }
        }
        
        // Entraîner le modèle si nécessaire
        trainModelForProduct(produitId);
        
        // Générer les prévisions
        List<ForecastPoint> forecasts = forecastCache.get(produitId);
        if (forecasts == null || forecasts.isEmpty() || Double.isNaN(forecasts.get(0).predictedValue())) {
            return generateDefaultForecast(produitId, days);
        }
        
        return forecasts.subList(0, Math.min(days, forecasts.size()));
    }

    /**
     * Génère des prévisions par défaut quand pas assez de données historiques
     */
    private List<ForecastPoint> generateDefaultForecast(Long produitId, int days) {
        List<ForecastPoint> forecasts = new ArrayList<>();
        LocalDate startDate = LocalDate.now().plusDays(1);
        
        // Valeur par défaut: 5 ventes par jour avec variation saisonnière
        double baseValue = 5.0;
        
        for (int i = 1; i <= days; i++) {
            LocalDate date = startDate.plusDays(i - 1);
            int dayOfWeek = date.getDayOfWeek().getValue();
            
            // Week-ends: +30%
            double seasonalFactor = (dayOfWeek >= 6) ? 1.3 : 1.0;
            double predictedValue = baseValue * seasonalFactor;
            double confidenceInterval = predictedValue * 0.3; // 30% de marge
            
            double confidence = Math.max(0.5, 0.95 - (i * 0.02)); // Décroissance de confiance
            
            forecasts.add(new ForecastPoint(
                date,
                Math.round(predictedValue * 10.0) / 10.0,
                Math.max(0, Math.round((predictedValue - confidenceInterval) * 10.0) / 10.0),
                Math.round((predictedValue + confidenceInterval) * 10.0) / 10.0,
                Math.round(confidence * 100.0) / 100.0
            ));
        }
        
        return forecasts;
    }

    /**
     * Analyse les risques de rupture de stock pour tous les produits
     */
    @Transactional(readOnly = true)
    public List<StockRiskAlert> analyzeAllStockRisks() {
        List<StockRiskAlert> risks = new ArrayList<>();
        List<Stock> allStocks = stockRepository.findAll();
        
        for (Stock stock : allStocks) {
            if (stock.getProduit() == null) continue;
            
            Long produitId = stock.getProduit().getProduitId();
            List<StockRisk> productRisks = analyzeStockRisk(produitId, stock.getQuantite());
            
            for (StockRisk risk : productRisks) {
                risks.add(new StockRiskAlert(
                    produitId,
                    stock.getProduit().getNom(),
                    risk.level(),
                    risk.message(),
                    risk.currentStock(),
                    risk.predictedDemand(),
                    risk.leadTimeDays(),
                    risk.recommendation()
                ));
            }
        }
        
        // Trier par niveau de risque (CRITICAL d'abord)
        risks.sort((a, b) -> b.riskLevel().ordinal() - a.riskLevel().ordinal());
        
        return risks;
    }

    /**
     * Analyse le risque de rupture pour un produit spécifique
     */
    public List<StockRisk> analyzeStockRisk(Long produitId, int currentStock) {
        // Assurer que le modèle est entraîné
        if (!isCacheValid(produitId)) {
            trainModelForProduct(produitId);
        }
        
        // Lead time par défaut: 7 jours
        return forecaster.predictStockRisks(currentStock, 7);
    }

    /**
     * Obtient l'analyse de tendance pour un produit
     */
    public TrendAnalysis getTrendAnalysis(Long produitId) {
        List<TimePoint> salesData = getHistoricalSales(produitId, 60);
        
        if (salesData.size() < 14) {
            // Retourner une analyse par défaut
            return new TrendAnalysis(
                0.0,
                0.5,
                "Pas assez de données historiques pour l'analyse de tendance"
            );
        }
        
        return forecaster.analyzeTrend(salesData);
    }

    /**
     * Calcule le niveau de stock optimal (point de commande)
     */
    public double calculateOptimalStockLevel(Long produitId, int leadTimeDays, 
                                               double safetyStockPercent) {
        // Assurer que le modèle est entraîné
        if (!isCacheValid(produitId)) {
            trainModelForProduct(produitId);
        }
        
        return forecaster.calculateOptimalStockLevel(leadTimeDays, safetyStockPercent);
    }

    /**
     * Génère un rapport de prévision complet pour un produit
     */
    public ForecastReport generateForecastReport(Long produitId) {
        Produit produit = produitRepository.findById(produitId).orElse(null);
        if (produit == null) {
            return null;
        }
        
        // Données historiques
        List<TimePoint> historicalData = getHistoricalSales(produitId, 90);
        
        // Prévisions sur 30 jours
        List<ForecastPoint> forecasts = getDemandForecast(produitId, 30);
        
        // Analyse de tendance
        TrendAnalysis trend = getTrendAnalysis(produitId);
        
        // Analyse des risques
        Stock stock = stockRepository.findByProduitProduitId(produitId).orElse(null);
        int currentStock = stock != null ? stock.getQuantite() : 0;
        List<StockRisk> risks = analyzeStockRisk(produitId, currentStock);
        
        // Calculer la demande totale prévue sur 30 jours
        double totalPredictedDemand = forecasts.stream()
                .mapToDouble(ForecastPoint::predictedValue)
                .sum();
        
        // Calculer le niveau de stock optimal
        double optimalStock = calculateOptimalStockLevel(produitId, 7, 20);
        
        return new ForecastReport(
            produitId,
            produit.getNom(),
            historicalData.size(),
            forecasts,
            trend,
            risks,
            currentStock,
            totalPredictedDemand,
            optimalStock,
            currentStock < totalPredictedDemand * 0.5 ? "COMMANDE URGENTE RECOMMANDÉE" : "OK"
        );
    }

    /**
     * Obtient les produits avec les ventes prévues les plus élevées
     */
    public List<ProductForecastSummary> getTopPredictedProducts(int limit) {
        List<Produit> produits = produitRepository.findByActifTrue();
        List<ProductForecastSummary> summaries = new ArrayList<>();
        
        for (Produit produit : produits) {
            List<ForecastPoint> forecast = getDemandForecast(produit.getProduitId(), 7);
            double weeklyDemand = forecast.stream()
                    .mapToDouble(ForecastPoint::predictedValue)
                    .sum();
            
            summaries.add(new ProductForecastSummary(
                produit.getProduitId(),
                produit.getNom(),
                weeklyDemand,
                getTrendAnalysis(produit.getProduitId()).trendPercent()
            ));
        }
        
        // Trier par demande prévue décroissante
        summaries.sort((a, b) -> Double.compare(b.predictedWeeklyDemand(), a.predictedWeeklyDemand()));
        
        return summaries.size() > limit ? summaries.subList(0, limit) : summaries;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private List<TimePoint> getHistoricalSales(Long produitId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        
        // Récupérer les lignes de commande pour ce produit
        List<LigneCommande> lignes = ligneCommandeRepository.findByProduitProduitId(produitId);
        
        // Agréger par jour
        Map<LocalDate, Double> dailySales = new HashMap<>();
        
        for (LigneCommande ligne : lignes) {
            if (ligne.getCommande() == null || ligne.getCommande().getDate() == null) {
                continue;
            }
            
            LocalDate date = ligne.getCommande().getDate().toLocalDate();
            
            if (date.isBefore(startDate)) {
                continue;
            }
            
            double quantity = ligne.getQuantite() != null ? ligne.getQuantite() : 0;
            dailySales.merge(date, quantity, Double::sum);
        }
        
        // Créer les points de série temporelle (remplir les jours manquants avec 0)
        List<TimePoint> timePoints = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            double value = dailySales.getOrDefault(date, 0.0);
            timePoints.add(new TimePoint(date, value));
        }
        
        return timePoints;
    }

    private boolean isCacheValid(Long produitId) {
        LocalDateTime timestamp = cacheTimestamps.get(produitId);
        if (timestamp == null) return false;
        
        long hoursSinceUpdate = ChronoUnit.HOURS.between(timestamp, LocalDateTime.now());
        return hoursSinceUpdate < CACHE_VALIDITY_HOURS;
    }

    // ==================== CLASSES DE DONNÉES ====================

    public record StockRiskAlert(
        Long produitId,
        String produitNom,
        StockRiskLevel riskLevel,
        String message,
        double currentStock,
        double predictedDemand,
        int leadTimeDays,
        String recommendation
    ) {}

    public record ForecastReport(
        Long produitId,
        String produitNom,
        int historicalDataPoints,
        List<ForecastPoint> forecasts,
        TrendAnalysis trendAnalysis,
        List<StockRisk> stockRisks,
        double currentStock,
        double totalPredictedDemand30Days,
        double optimalStockLevel,
        String recommendation
    ) {}

    public record ProductForecastSummary(
        Long produitId,
        String produitNom,
        double predictedWeeklyDemand,
        double trendPercent
    ) {}
}
