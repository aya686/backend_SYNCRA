package com.synchub.ms6.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour l'activation des tâches planifiées ML
 * Les modèles sont réentraînés automatiquement chaque nuit
 */
@Configuration
@EnableScheduling
public class MLSchedulingConfig {
    // L'activation des scheduling est faite via @EnableScheduling
    // Les tâches planifiées sont définies dans les services ML
    // 
    // Schedule:
    // - 02:00: Réentraînement du modèle de prédiction de retards de livraison
    // - 03:00: Réentraînement du système de recommandation
    // - 04:00: Réentraînement des modèles de prévision de demande
}
