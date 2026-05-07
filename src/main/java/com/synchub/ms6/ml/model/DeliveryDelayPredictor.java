package com.synchub.ms6.ml.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Modèle de prédiction de retards de livraison utilisant la Régression Logistique
 * Implémentation from scratch en Java - Pas de bibliothèque externe ML
 * 
 * Algorithme: Régression Logistique avec descente de gradient
 * Features: température, condition météo, transporteur, jour de la semaine, saison
 */
@Component
@Slf4j
public class DeliveryDelayPredictor implements Serializable {

    private static final long serialVersionUID = 1L;

    // Poids du modèle (coefficients)
    private double[] weights;
    private double bias;
    
    // Statistiques pour normalisation
    private double[] featureMeans;
    private double[] featureStdDevs;
    
    // Métriques d'entraînement
    private double trainingAccuracy;
    private int trainingSamples;
    private int epochsCompleted;
    
    // Paramètres d'entraînement
    private static final double LEARNING_RATE = 0.01;
    private static final int EPOCHS = 1000;
    private static final double REGULARIZATION = 0.001;

    public DeliveryDelayPredictor() {
        this.weights = new double[8]; // 8 features
        this.bias = 0.0;
        this.featureMeans = new double[8];
        this.featureStdDevs = new double[8];
        Arrays.fill(featureStdDevs, 1.0); // Éviter division par zéro
    }

    /**
     * Structure de données pour une instance d'entraînement
     */
    public static class TrainingInstance {
        public final double[] features; // 8 features normalisées
        public final boolean delayed;   // true = retard, false = à l'heure

        public TrainingInstance(double[] features, boolean delayed) {
            this.features = features;
            this.delayed = delayed;
        }
    }

    /**
     * Entraîne le modèle sur les données historiques
     * Algorithme: Descente de gradient stochastique avec régularisation L2
     */
    public void train(TrainingInstance[] trainingData) {
        if (trainingData == null || trainingData.length < 10) {
            log.warn("Pas assez de données pour entraîner le modèle (min 10 requis)");
            return;
        }

        log.info("Démarrage de l'entraînement sur {} échantillons...", trainingData.length);
        
        // Calculer moyennes et écarts-types pour normalisation
        calculateNormalizationStats(trainingData);
        
        // Normaliser les données
        double[][] normalizedFeatures = new double[trainingData.length][8];
        int[] labels = new int[trainingData.length];
        
        for (int i = 0; i < trainingData.length; i++) {
            normalizedFeatures[i] = normalize(trainingData[i].features);
            labels[i] = trainingData[i].delayed ? 1 : 0;
        }

        // Descente de gradient
        int n = trainingData.length;
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            double totalLoss = 0.0;
            
            for (int i = 0; i < n; i++) {
                double[] x = normalizedFeatures[i];
                int y = labels[i];
                
                // Prédiction
                double z = dotProduct(weights, x) + bias;
                double prediction = sigmoid(z);
                
                // Calcul de l'erreur (Binary Cross-Entropy)
                double error = prediction - y;
                totalLoss += - (y * Math.log(prediction + 1e-10) + (1-y) * Math.log(1 - prediction + 1e-10));
                
                // Mise à jour des poids (avec régularisation L2)
                for (int j = 0; j < weights.length; j++) {
                    weights[j] -= LEARNING_RATE * (error * x[j] + REGULARIZATION * weights[j]);
                }
                bias -= LEARNING_RATE * error;
            }
            
            if (epoch % 100 == 0) {
                log.debug("Epoch {} - Loss: {}", epoch, totalLoss / n);
            }
        }
        
        // Calculer la précision sur les données d'entraînement
        trainingAccuracy = evaluateAccuracy(normalizedFeatures, labels);
        trainingSamples = trainingData.length;
        epochsCompleted = EPOCHS;
        
        log.info("Entraînement terminé! Précision: {:.2f}% sur {} échantillons", 
                 trainingAccuracy * 100, trainingSamples);
    }

    /**
     * Prédit si une livraison sera retardée
     * @param features tableau de 8 features: [temp, isRain, isSnow, isStorm, 
     *                 transporteur_encoded, jour_semaine, saison, distance_km]
     * @return probabilité de retard (0.0 à 1.0)
     */
    public double predictDelayProbability(double[] features) {
        if (features == null || features.length != 8) {
            throw new IllegalArgumentException("Features doit avoir exactement 8 valeurs");
        }
        
        double[] normalized = normalize(features);
        double z = dotProduct(weights, normalized) + bias;
        return sigmoid(z);
    }

    /**
     * Prédit avec classification binaire
     */
    public boolean predictDelay(double[] features) {
        return predictDelayProbability(features) > 0.5;
    }

    /**
     * Calcule le niveau de risque
     */
    public RiskLevel getRiskLevel(double[] features) {
        double prob = predictDelayProbability(features);
        if (prob < 0.3) return RiskLevel.LOW;
        if (prob < 0.6) return RiskLevel.MEDIUM;
        if (prob < 0.8) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    public enum RiskLevel {
        LOW("Faible risque", "Conditions favorables"),
        MEDIUM("Risque modéré", "Surveillance recommandée"),
        HIGH("Risque élevé", "Préparation nécessaire"),
        CRITICAL("Risque critique", "Retard probable");

        public final String label;
        public final String description;

        RiskLevel(String label, String description) {
            this.label = label;
            this.description = description;
        }
    }

    // ==================== MÉTHODES MATHEMATIQUES ====================

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private void calculateNormalizationStats(TrainingInstance[] data) {
        int n = data.length;
        
        // Calculer moyennes
        Arrays.fill(featureMeans, 0.0);
        for (TrainingInstance instance : data) {
            for (int i = 0; i < 8; i++) {
                featureMeans[i] += instance.features[i];
            }
        }
        for (int i = 0; i < 8; i++) {
            featureMeans[i] /= n;
        }
        
        // Calculer écarts-types
        Arrays.fill(featureStdDevs, 0.0);
        for (TrainingInstance instance : data) {
            for (int i = 0; i < 8; i++) {
                double diff = instance.features[i] - featureMeans[i];
                featureStdDevs[i] += diff * diff;
            }
        }
        for (int i = 0; i < 8; i++) {
            featureStdDevs[i] = Math.sqrt(featureStdDevs[i] / n);
            if (featureStdDevs[i] == 0) featureStdDevs[i] = 1.0; // Éviter division par zéro
        }
    }

    private double[] normalize(double[] features) {
        double[] normalized = new double[8];
        for (int i = 0; i < 8; i++) {
            normalized[i] = (features[i] - featureMeans[i]) / featureStdDevs[i];
        }
        return normalized;
    }

    private double evaluateAccuracy(double[][] features, int[] labels) {
        int correct = 0;
        for (int i = 0; i < features.length; i++) {
            double z = dotProduct(weights, features[i]) + bias;
            double pred = sigmoid(z);
            boolean predicted = pred > 0.5;
            boolean actual = labels[i] == 1;
            if (predicted == actual) correct++;
        }
        return (double) correct / features.length;
    }

    // ==================== GETTERS ====================

    public double getTrainingAccuracy() {
        return trainingAccuracy;
    }

    public int getTrainingSamples() {
        return trainingSamples;
    }

    public double[] getWeights() {
        return weights.clone();
    }

    public double getBias() {
        return bias;
    }

    public boolean isTrained() {
        return trainingSamples > 0;
    }
}
