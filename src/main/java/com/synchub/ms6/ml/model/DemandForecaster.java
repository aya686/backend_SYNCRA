package com.synchub.ms6.ml.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * Modèle de prévision de demande (Time Series Forecasting)
 * Utilise la méthode de lissage exponentiel de Holt-Winters
 * pour capturer tendance et saisonnalité
 * 
 * Implémentation from scratch en Java
 */
@Component
@Slf4j
public class DemandForecaster implements Serializable {

    private static final long serialVersionUID = 1L;

    // Paramètres du modèle Holt-Winters
    private double alpha; // Lissage du niveau (0-1)
    private double beta;  // Lissage de la tendance (0-1)
    private double gamma; // Lissage de la saisonnalité (0-1)
    
    // Période de saisonnalité (par défaut: 7 jours pour effet hebdomadaire)
    private int seasonalityPeriod;
    
    // Composantes du modèle
    private double[] level;      // Niveau
    private double[] trend;      // Tendance
    private double[] seasonal;   // Indices saisonniers
    
    // Historique pour validation
    private double trainingError;
    private int forecastHorizon;

    public DemandForecaster() {
        this.alpha = 0.3;
        this.beta = 0.1;
        this.gamma = 0.1;
        this.seasonalityPeriod = 7; // Saisonnalité hebdomadaire
        this.forecastHorizon = 14;  // Prévision sur 14 jours par défaut
    }

    /**
     * Structure de données pour un point de série temporelle
     */
    public static class TimePoint {
        public final LocalDate date;
        public final double value;  // Quantité vendue ou demande

        public TimePoint(LocalDate date, double value) {
            this.date = date;
            this.value = value;
        }
    }

    /**
     * Entraîne le modèle sur les données historiques
     * Algorithme: Holt-Winters Triple Exponential Smoothing
     */
    public void train(List<TimePoint> historicalData) {
        if (historicalData == null || historicalData.size() < seasonalityPeriod * 2) {
            log.warn("Pas assez de données pour entraîner le modèle (min {} requis)", 
                     seasonalityPeriod * 2);
            return;
        }

        // Trier par date
        historicalData.sort(Comparator.comparing(tp -> tp.date));
        
        int n = historicalData.size();
        double[] values = historicalData.stream()
                .mapToDouble(tp -> tp.value)
                .toArray();

        log.info("Entraînement du modèle de prévision sur {} points...", n);

        // Initialisation
        initializeComponents(values);

        // Optimisation des paramètres par grid search
        optimizeParameters(values);

        // Entraînement final avec les meilleurs paramètres
        fitModel(values);

        // Calculer l'erreur de prédiction (RMSE)
        trainingError = calculateRMSE(values);

        log.info("Modèle entraîné - Alpha: {:.3f}, Beta: {:.3f}, Gamma: {:.3f}, RMSE: {:.2f}",
                 alpha, beta, gamma, trainingError);
    }

    /**
     * Prédit la demande pour les prochains jours
     */
    public List<ForecastPoint> forecast(int days) {
        if (level == null || level.length == 0) {
            log.warn("Modèle non entraîné");
            return new ArrayList<>();
        }

        List<ForecastPoint> forecasts = new ArrayList<>();
        int lastIdx = level.length - 1;
        
        // Date de début (aujourd'hui + 1)
        LocalDate startDate = LocalDate.now().plusDays(1);

        for (int i = 1; i <= days; i++) {
            int seasonIdx = (lastIdx + i) % seasonalityPeriod;
            
            // Formule Holt-Winters: F(t+m) = [L(t) + m*T(t)] * S(t+m-s)
            double forecast = (level[lastIdx] + i * trend[lastIdx]) * seasonal[seasonIdx];
            
            // Calculer l'intervalle de confiance (±2 écarts-types)
            double confidenceInterval = 2 * trainingError * Math.sqrt(i);
            
            forecasts.add(new ForecastPoint(
                startDate.plusDays(i - 1),
                Math.max(0, forecast),
                Math.max(0, forecast - confidenceInterval),
                forecast + confidenceInterval,
                calculateConfidence(i)
            ));
        }

        return forecasts;
    }

    /**
     * Prédit la demande pour un jour spécifique
     */
    public ForecastPoint forecastForDate(LocalDate date) {
        if (level == null || level.length == 0) {
            return null;
        }

        LocalDate today = LocalDate.now();
        int daysAhead = (int) java.time.temporal.ChronoUnit.DAYS.between(today, date);
        
        if (daysAhead <= 0) {
            return null; // Ne pas prédire le passé
        }

        List<ForecastPoint> forecasts = forecast(daysAhead);
        return forecasts.isEmpty() ? null : forecasts.get(forecasts.size() - 1);
    }

    /**
     * Détecte les produits à risque de rupture de stock
     * Compare la prévision avec le stock actuel
     */
    public List<StockRisk> predictStockRisks(double currentStock, int leadTimeDays) {
        List<ForecastPoint> forecasts = forecast(leadTimeDays);
        
        // Calculer la demande cumulée pendant le délai de réapprovisionnement
        double totalDemand = forecasts.stream()
                .mapToDouble(f -> f.predictedValue())
                .sum();
        
        List<StockRisk> risks = new ArrayList<>();
        
        if (currentStock < totalDemand * 0.5) {
            risks.add(new StockRisk(
                StockRiskLevel.CRITICAL,
                "Rupture de stock imminente",
                currentStock,
                totalDemand,
                leadTimeDays,
                "Commander immédiatement"
            ));
        } else if (currentStock < totalDemand * 0.8) {
            risks.add(new StockRisk(
                StockRiskLevel.HIGH,
                "Risque de rupture élevé",
                currentStock,
                totalDemand,
                leadTimeDays,
                "Planifier commande"
            ));
        } else if (currentStock < totalDemand * 1.2) {
            risks.add(new StockRisk(
                StockRiskLevel.MEDIUM,
                "Surveillance recommandée",
                currentStock,
                totalDemand,
                leadTimeDays,
                "Monitorer les stocks"
            ));
        }
        
        return risks;
    }

    /**
     * Analyse la tendance et saisonnalité
     */
    public TrendAnalysis analyzeTrend(List<TimePoint> historicalData) {
        if (historicalData == null || historicalData.size() < 14) {
            return new TrendAnalysis(0, 0, "Données insuffisantes");
        }

        // Calculer la tendance moyenne sur 7 derniers jours
        int n = historicalData.size();
        double recentAvg = historicalData.subList(n - 7, n).stream()
                .mapToDouble(tp -> tp.value)
                .average()
                .orElse(0);
        
        double previousAvg = historicalData.subList(Math.max(0, n - 14), n - 7).stream()
                .mapToDouble(tp -> tp.value)
                .average()
                .orElse(0);
        
        double trendPercent = previousAvg > 0 ? 
                ((recentAvg - previousAvg) / previousAvg) * 100 : 0;
        
        String trendDescription;
        if (trendPercent > 10) {
            trendDescription = "Tendance haussière forte (+" + String.format("%.1f", trendPercent) + "%)";
        } else if (trendPercent > 0) {
            trendDescription = "Tendance haussière modérée (+" + String.format("%.1f", trendPercent) + "%)";
        } else if (trendPercent > -10) {
            trendDescription = "Tendance baissière modérée (" + String.format("%.1f", trendPercent) + "%)";
        } else {
            trendDescription = "Tendance baissière forte (" + String.format("%.1f", trendPercent) + "%)";
        }

        // Détecter saisonnalité hebdomadaire
        double seasonalityStrength = detectSeasonality(historicalData);

        return new TrendAnalysis(trendPercent, seasonalityStrength, trendDescription);
    }

    /**
     * Calcule le niveau de stock optimal (reorder point)
     */
    public double calculateOptimalStockLevel(int leadTimeDays, double safetyStockPercent) {
        List<ForecastPoint> forecasts = forecast(leadTimeDays);
        
        double avgDemand = forecasts.stream()
                .mapToDouble(f -> f.predictedValue())
                .average()
                .orElse(0);
        
        double demandStd = calculateStdDev(forecasts.stream()
                .mapToDouble(f -> f.predictedValue())
                .toArray());
        
        // Formule: Lead Time Demand + Safety Stock
        double leadTimeDemand = avgDemand * leadTimeDays;
        double safetyStock = demandStd * safetyStockPercent / 100;
        
        return leadTimeDemand + safetyStock;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void initializeComponents(double[] values) {
        int n = values.length;
        level = new double[n];
        trend = new double[n];
        seasonal = new double[seasonalityPeriod];

        // Initialiser les indices saisonniers
        double[] seasonSums = new double[seasonalityPeriod];
        int[] seasonCounts = new int[seasonalityPeriod];
        
        for (int i = 0; i < n; i++) {
            int seasonIdx = i % seasonalityPeriod;
            seasonSums[seasonIdx] += values[i];
            seasonCounts[seasonIdx]++;
        }
        
        double totalAvg = Arrays.stream(values).sum() / n;
        for (int i = 0; i < seasonalityPeriod; i++) {
            seasonal[i] = seasonCounts[i] > 0 ? 
                    (seasonSums[i] / seasonCounts[i]) / totalAvg : 1.0;
        }

        // Initialiser niveau et tendance
        level[0] = values[0] / seasonal[0];
        trend[0] = (values[seasonalityPeriod] / seasonal[0] - values[0] / seasonal[0]) 
                   / seasonalityPeriod;
    }

    private void optimizeParameters(double[] values) {
        // Grid search simple pour trouver les meilleurs paramètres
        double bestError = Double.MAX_VALUE;
        double bestAlpha = alpha, bestBeta = beta, bestGamma = gamma;

        for (double a = 0.1; a <= 0.5; a += 0.1) {
            for (double b = 0.05; b <= 0.3; b += 0.05) {
                for (double g = 0.05; g <= 0.3; g += 0.05) {
                    double error = tryParameters(values, a, b, g);
                    if (error < bestError) {
                        bestError = error;
                        bestAlpha = a;
                        bestBeta = b;
                        bestGamma = g;
                    }
                }
            }
        }

        alpha = bestAlpha;
        beta = bestBeta;
        gamma = bestGamma;
    }

    private double tryParameters(double[] values, double a, double b, double g) {
        // Version simplifiée pour test des paramètres
        double[] testLevel = new double[values.length];
        double[] testTrend = new double[values.length];
        
        testLevel[0] = values[0];
        testTrend[0] = values[1] - values[0];
        
        double sumSquaredError = 0;
        
        for (int t = 1; t < values.length; t++) {
            int seasonIdx = t % seasonalityPeriod;
            
            // Mise à jour du niveau
            testLevel[t] = a * (values[t] / seasonal[seasonIdx]) + 
                          (1 - a) * (testLevel[t-1] + testTrend[t-1]);
            
            // Mise à jour de la tendance
            testTrend[t] = b * (testLevel[t] - testLevel[t-1]) + 
                          (1 - b) * testTrend[t-1];
            
            // Prédiction
            if (t > seasonalityPeriod) {
                double forecast = (testLevel[t-1] + testTrend[t-1]) * seasonal[seasonIdx];
                double error = values[t] - forecast;
                sumSquaredError += error * error;
            }
        }
        
        return Math.sqrt(sumSquaredError / (values.length - seasonalityPeriod));
    }

    private void fitModel(double[] values) {
        int n = values.length;
        
        for (int t = 1; t < n; t++) {
            int seasonIdx = t % seasonalityPeriod;
            
            // Mise à jour du niveau
            level[t] = alpha * (values[t] / seasonal[seasonIdx]) + 
                      (1 - alpha) * (level[t-1] + trend[t-1]);
            
            // Mise à jour de la tendance
            trend[t] = beta * (level[t] - level[t-1]) + 
                      (1 - beta) * trend[t-1];
            
            // Mise à jour de la saisonnalité
            seasonal[seasonIdx] = gamma * (values[t] / level[t]) + 
                                   (1 - gamma) * seasonal[seasonIdx];
        }
    }

    private double calculateRMSE(double[] actual) {
        double sumSquaredError = 0;
        int count = 0;
        
        for (int t = seasonalityPeriod; t < actual.length; t++) {
            int seasonIdx = t % seasonalityPeriod;
            double forecast = (level[t-1] + trend[t-1]) * seasonal[seasonIdx];
            double error = actual[t] - forecast;
            sumSquaredError += error * error;
            count++;
        }
        
        return count > 0 ? Math.sqrt(sumSquaredError / count) : 0;
    }

    private double calculateConfidence(int daysAhead) {
        // Confiance diminue avec l'horizon de prédiction
        return Math.max(0.3, 1.0 - (daysAhead * 0.02));
    }

    private double detectSeasonality(List<TimePoint> data) {
        // Calculer la variance expliquée par la saisonnalité hebdomadaire
        double[] byDayOfWeek = new double[7];
        int[] countByDay = new int[7];
        
        for (TimePoint tp : data) {
            int dayOfWeek = tp.date.getDayOfWeek().getValue() - 1; // 0-6
            byDayOfWeek[dayOfWeek] += tp.value;
            countByDay[dayOfWeek]++;
        }
        
        // Calculer la moyenne par jour
        for (int i = 0; i < 7; i++) {
            if (countByDay[i] > 0) {
                byDayOfWeek[i] /= countByDay[i];
            }
        }
        
        // Variance entre jours (force de la saisonnalité)
        double mean = Arrays.stream(byDayOfWeek).sum() / 7;
        double variance = 0;
        for (double v : byDayOfWeek) {
            variance += Math.pow(v - mean, 2);
        }
        
        return Math.min(1.0, variance / (mean * mean)); // Normalisé 0-1
    }

    private double calculateStdDev(double[] values) {
        if (values.length == 0) return 0;
        
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .sum() / values.length;
        
        return Math.sqrt(variance);
    }

    // ==================== CLASSES DE DONNÉES ====================

    public record ForecastPoint(
        LocalDate date,
        double predictedValue,
        double lowerBound,
        double upperBound,
        double confidence
    ) {
        public boolean isInConfidenceInterval(double actual) {
            return actual >= lowerBound && actual <= upperBound;
        }
    }

    public record StockRisk(
        StockRiskLevel level,
        String message,
        double currentStock,
        double predictedDemand,
        int leadTimeDays,
        String recommendation
    ) {}

    public record TrendAnalysis(
        double trendPercent,
        double seasonalityStrength,
        String description
    ) {}

    public enum StockRiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public ModelStats getStats() {
        return new ModelStats(
            alpha, beta, gamma, seasonalityPeriod,
            trainingError, level != null && level.length > 0
        );
    }

    public record ModelStats(
        double alpha,
        double beta,
        double gamma,
        int seasonalityPeriod,
        double rmse,
        boolean trained
    ) {}
}
