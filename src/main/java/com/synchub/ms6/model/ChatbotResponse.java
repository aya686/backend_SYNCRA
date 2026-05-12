package com.synchub.ms6.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Réponse du chatbot NLP avec métadonnées complètes
 * Inclut l'intention détectée, la réponse générée, les entités extraites,
 * le score de sentiment et des suggestions de follow-up
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    
    /**
     * Intention détectée par l'algorithme TF-IDF + Similarité Cosinus
     */
    private ChatbotIntent intent;
    
    /**
     * Description lisible de l'intention
     */
    private String intentDescription;
    
    /**
     * Réponse textuelle générée pour l'utilisateur
     */
    private String response;
    
    /**
     * Entités extraites du message (NER)
     * Ex: produitId, commandeId, produitNom, prixMentionne
     */
    private Map<String, String> entities;
    
    /**
     * Score de sentiment entre -1 (négatif) et 1 (positif)
     * 0 = neutre
     */
    private double sentimentScore;
    
    /**
     * Description du sentiment
     */
    private String sentimentLabel;
    
    /**
     * Score de confiance de la classification (0 à 1)
     * Plus proche de 1 = plus confiant
     */
    private double confidence;
    
    /**
     * Suggestions de follow-up pour guider la conversation
     */
    private List<String> suggestions;
    
    /**
     * Message original de l'utilisateur
     */
    private String originalMessage;
    
    /**
     * ID utilisateur
     */
    private Long userId;
    
    /**
     * Timestamp de la réponse
     */
    private Date timestamp;
    
    /**
     * Temps de traitement en millisecondes
     */
    private long processingTimeMs;
    
    /**
     * Méthode utilitaire pour obtenir le label du sentiment
     */
    public String getSentimentLabel() {
        if (sentimentScore > 0.3) return "POSITIF 😊";
        if (sentimentScore < -0.3) return "NÉGATIF 😞";
        return "NEUTRE 😐";
    }
    
    /**
     * Méthode utilitaire pour formater le score de confiance
     */
    public String getConfidenceFormatted() {
        return String.format("%.1f%%", confidence * 100);
    }
    
    /**
     * Vérifier si la confiance est suffisante
     */
    public boolean isHighConfidence() {
        return confidence > 0.7;
    }
    
    /**
     * Vérifier si c'est une intention de réclamation
     */
    public boolean isComplaint() {
        return intent == ChatbotIntent.COMPLAINT || sentimentScore < -0.5;
    }
}
