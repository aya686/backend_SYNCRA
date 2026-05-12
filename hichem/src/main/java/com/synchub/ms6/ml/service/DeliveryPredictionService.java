package com.synchub.ms6.ml.service;

import com.synchub.ms6.entity.Livraison;
import com.synchub.ms6.ml.model.DeliveryDelayPredictor;
import com.synchub.ms6.ml.model.DeliveryDelayPredictor.TrainingInstance;
import com.synchub.ms6.repository.LivraisonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de prédiction de retards de livraison
 * Utilise un modèle ML entraîné sur les données historiques de livraisons
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryPredictionService {

    private final LivraisonRepository livraisonRepository;
    private final DeliveryDelayPredictor predictor;

    // Mapping des transporteurs vers codes numériques
    private final Map<String, Integer> transporteurMapping = new HashMap<>();
    private int transporteurCounter = 0;

    /**
     * Entraîne le modèle avec les données historiques de livraisons
     * Exécuté automatiquement toutes les nuits à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *") // Tous les jours à 2h00
    @Transactional(readOnly = true)
    public void retrainModel() {
        log.info("Démarrage de l'entraînement du modèle de prédiction de retards...");
        
        List<Livraison> livraisons = livraisonRepository.findAll();
        
        if (livraisons.size() < 20) {
            log.warn("Pas assez de données pour entraîner le modèle ({} livraisons, min 20 requis)", 
                     livraisons.size());
            return;
        }

        // Filtrer les livraisons livrées (avec date de livraison connue)
        List<Livraison> deliveredShipments = livraisons.stream()
                .filter(l -> l.getDateLiv() != null && l.getDateExp() != null)
                .toList();

        if (deliveredShipments.size() < 20) {
            log.warn("Pas assez de livraisons terminées pour l'entraînement ({} requis)", 
                     deliveredShipments.size());
            return;
        }

        // Créer les instances d'entraînement
        List<TrainingInstance> instances = new ArrayList<>();
        
        for (Livraison livraison : deliveredShipments) {
            TrainingInstance instance = createTrainingInstance(livraison);
            if (instance != null) {
                instances.add(instance);
            }
        }

        if (instances.size() < 10) {
            log.warn("Pas assez d'instances valides pour l'entraînement");
            return;
        }

        // Entraîner le modèle
        TrainingInstance[] trainingData = instances.toArray(new TrainingInstance[0]);
        predictor.train(trainingData);
        
        log.info("Modèle réentraîné avec succès! {} échantillons utilisés", instances.size());
    }

    /**
     * Prédit le risque de retard pour une livraison future
     */
    public DelayPrediction predictForNewDelivery(String city, String transporteur, 
                                                   LocalDateTime dateExpedition) {
        // Construire le vecteur de features
        double[] features = buildFeatures(city, transporteur, dateExpedition);
        
        // Vérifier si le modèle est entraîné
        if (!predictor.isTrained()) {
            log.warn("Modèle non entraîné - retour d'une prédiction par défaut");
            return new DelayPrediction(0.3, DeliveryDelayPredictor.RiskLevel.MEDIUM, 
                                       "Modèle en cours d'entraînement");
        }
        
        // Prédire
        double probability = predictor.predictDelayProbability(features);
        DeliveryDelayPredictor.RiskLevel riskLevel = predictor.getRiskLevel(features);
        
        String explanation = generateExplanation(features, probability, riskLevel);
        
        return new DelayPrediction(probability, riskLevel, explanation);
    }

    /**
     * Analyse les facteurs de risque pour une livraison
     */
    public RiskAnalysis analyzeRiskFactors(String city, String transporteur, 
                                            LocalDateTime dateExpedition) {
        double[] features = buildFeatures(city, transporteur, dateExpedition);
        
        List<RiskFactor> factors = new ArrayList<>();
        
        // Analyser chaque facteur
        if (features[1] == 1.0) { // Pluie
            factors.add(new RiskFactor("Conditions pluvieuses", 
                         "La pluie augmente le risque de retard de 25%", 0.25));
        }
        if (features[2] == 1.0) { // Neige
            factors.add(new RiskFactor("Conditions neigeuses", 
                         "La neige augmente significativement le risque", 0.40));
        }
        if (features[3] == 1.0) { // Orage
            factors.add(new RiskFactor("Conditions orageuses", 
                         "Orages = risque élevé de retard", 0.50));
        }
        if (features[6] == 3.0) { // Hiver
            factors.add(new RiskFactor("Saison hivernale", 
                         "Les livraisons sont plus lentes en hiver", 0.15));
        }
        if (features[4] > 2.0) { // Transporteur avec historique défavorable
            factors.add(new RiskFactor("Historique transporteur", 
                         "Ce transporteur a des retards fréquents", 0.20));
        }
        
        return new RiskAnalysis(factors, factors.stream().mapToDouble(f -> f.impact).sum());
    }

    /**
     * Obtient les statistiques du modèle
     */
    public ModelStats getModelStatistics() {
        if (!predictor.isTrained()) {
            return new ModelStats(false, 0, 0.0, "Modèle non entraîné");
        }
        
        return new ModelStats(true, predictor.getTrainingSamples(), 
                             predictor.getTrainingAccuracy(), 
                             "Modèle opérationnel");
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private TrainingInstance createTrainingInstance(Livraison livraison) {
        try {
            // Déterminer si retardé (livraison après date prévue + 24h de marge)
            boolean isDelayed = isDeliveryDelayed(livraison);
            
            // Extraire les features
            double[] features = extractFeatures(livraison);
            
            return new TrainingInstance(features, isDelayed);
        } catch (Exception e) {
            log.warn("Erreur lors de la création de l'instance d'entraînement: {}", e.getMessage());
            return null;
        }
    }

    private boolean isDeliveryDelayed(Livraison livraison) {
        // Considérer comme retardée si livraison > 48h après expédition
        // ou si conditions météo défavorables avec délai anormal
        LocalDateTime dateExp = livraison.getDateExp();
        LocalDateTime dateLiv = livraison.getDateLiv();
        
        if (dateExp == null || dateLiv == null) return false;
        
        long hours = ChronoUnit.HOURS.between(dateExp, dateLiv);
        
        // Retard si > 48h OU conditions météo défavorables avec délai > 24h
        boolean badWeather = livraison.getWeatherAlert() != null && livraison.getWeatherAlert();
        
        return hours > 48 || (badWeather && hours > 24);
    }

    private double[] extractFeatures(Livraison livraison) {
        double[] features = new double[8];
        
        // 1. Température
        features[0] = livraison.getWeatherTemp() != null ? livraison.getWeatherTemp() : 20.0;
        
        // 2-4. Conditions météo (binaires)
        String condition = livraison.getWeatherCondition() != null ? 
                          livraison.getWeatherCondition().toLowerCase() : "clear";
        features[1] = condition.contains("rain") ? 1.0 : 0.0;
        features[2] = condition.contains("snow") ? 1.0 : 0.0;
        features[3] = condition.contains("storm") || condition.contains("thunder") ? 1.0 : 0.0;
        
        // 5. Transporteur (encodé)
        features[4] = encodeTransporteur(livraison.getTransporteur());
        
        // 6. Jour de la semaine (1-7)
        LocalDateTime dateExp = livraison.getDateExp();
        if (dateExp != null) {
            features[5] = dateExp.getDayOfWeek().getValue();
        } else {
            features[5] = 1;
        }
        
        // 7. Saison (1:Printemps, 2:Été, 3:Automne, 4:Hiver)
        features[6] = getSeason(dateExp != null ? dateExp : LocalDateTime.now());
        
        // 8. Distance simulée (basée sur l'adresse - simplifié)
        features[7] = estimateDistance(livraison.getAdresse());
        
        return features;
    }

    private double[] buildFeatures(String city, String transporteur, LocalDateTime dateExp) {
        double[] features = new double[8];
        
        // Valeurs par défaut si pas de données météo
        features[0] = 20.0; // Température par défaut
        features[1] = 0.0; // Pas de pluie par défaut
        features[2] = 0.0; // Pas de neige
        features[3] = 0.0; // Pas d'orage
        
        // Transporteur
        features[4] = encodeTransporteur(transporteur);
        
        // Jour de la semaine
        features[5] = dateExp.getDayOfWeek().getValue();
        
        // Saison
        features[6] = getSeason(dateExp);
        
        // Distance estimée
        features[7] = estimateDistance(city);
        
        return features;
    }

    private double encodeTransporteur(String transporteur) {
        if (transporteur == null) return 0.0;
        
        return transporteurMapping.computeIfAbsent(transporteur.toLowerCase(), 
                                                    k -> transporteurCounter++);
    }

    private int getSeason(LocalDateTime date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) return 1; // Printemps
        if (month >= 6 && month <= 8) return 2; // Été
        if (month >= 9 && month <= 11) return 3; // Automne
        return 4; // Hiver
    }

    private double estimateDistance(String address) {
        // Simulation simplifiée - en vrai utiliser géocodage
        if (address == null) return 100.0;
        
        String addr = address.toLowerCase();
        if (addr.contains("tunis")) return 50.0;
        if (addr.contains("sousse")) return 150.0;
        if (addr.contains("sfax")) return 250.0;
        if (addr.contains("paris")) return 500.0;
        return 200.0; // Distance par défaut
    }

    private String generateExplanation(double[] features, double probability, 
                                       DeliveryDelayPredictor.RiskLevel level) {
        StringBuilder sb = new StringBuilder();
        sb.append("Risque ").append(level.label.toLowerCase()).append(". ");
        
        if (features[1] == 1.0) sb.append("Présence de pluie. ");
        if (features[2] == 1.0) sb.append("Conditions neigeuses. ");
        if (features[3] == 1.0) sb.append("Orages signalés. ");
        if (features[6] == 4.0) sb.append("Période hivernale. ");
        
        sb.append("Probabilité de retard: ").append(String.format("%.1f%%", probability * 100));
        
        return sb.toString();
    }

    // ==================== CLASSES DE DONNÉES ====================

    public record DelayPrediction(double probability, 
                                   DeliveryDelayPredictor.RiskLevel riskLevel, 
                                   String explanation) {}

    public record RiskAnalysis(List<RiskFactor> factors, double totalImpact) {}

    public record RiskFactor(String name, String description, double impact) {}

    public record ModelStats(boolean trained, int samples, double accuracy, String status) {}
}
