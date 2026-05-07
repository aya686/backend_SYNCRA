package com.synchub.ms6.ml.service;

import com.synchub.ms6.entity.LigneCommande;
import com.synchub.ms6.entity.UserBehavior;
import com.synchub.ms6.ml.model.CollaborativeFilteringRecommender;
import com.synchub.ms6.ml.model.CollaborativeFilteringRecommender.*;
import com.synchub.ms6.repository.LigneCommandeRepository;
import com.synchub.ms6.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de recommandation de produits
 * Utilise le filtrage collaboratif pour recommander des produits personnalisés
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final CollaborativeFilteringRecommender recommender;
    private final UserBehaviorRepository userBehaviorRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    // Cache des recommandations pour améliorer les performances
    private final java.util.Map<Long, List<ProductRecommendation>> recommendationCache = new java.util.HashMap<>();
    private LocalDateTime lastCacheUpdate = LocalDateTime.MIN;

    /**
     * Initialisation au démarrage de l'application
     * Entraîne automatiquement le modèle si des données sont disponibles
     */
    @PostConstruct
    public void init() {
        log.info("🚀 Initialisation du service de recommandation...");
        
        // Vérifier les données disponibles
        long behaviorCount = userBehaviorRepository.count();
        long commandeCount = ligneCommandeRepository.count();
        
        log.info("📊 Données disponibles: {} comportements, {} lignes de commande", 
                 behaviorCount, commandeCount);
        
        // Lancer l'entraînement initial
        retrainModel();
    }

    /**
     * Entraîne le modèle de recommandation tous les jours à 3h du matin
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional(readOnly = true)
    public void retrainModel() {
        log.info("🎯 Démarrage de l'entraînement du système de recommandation...");

        // Collecter les interactions utilisateur
        List<UserProductInteraction> interactions = collectInteractions();
        
        log.info("📊 Interactions collectées: {}", interactions.size());

        if (interactions.size() < 20) {
            log.warn("⚠️ Pas assez d'interactions pour entraîner le modèle ({}/20 minimum)", 
                     interactions.size());
            log.warn("   Les recommandations seront basées sur la popularité uniquement.");
            return;
        }

        // Entraîner le modèle
        recommender.train(interactions);
        
        // Vider le cache
        recommendationCache.clear();
        lastCacheUpdate = LocalDateTime.now();

        // Statistiques
        RecommenderStats stats = recommender.getStats();
        log.info("Modèle de recommandation entraîné: {} utilisateurs, {} produits, {} interactions",
                 stats.numUsers(), stats.numProducts(), stats.totalInteractions());
    }

    /**
     * Obtient les recommandations personnalisées pour un utilisateur
     */
    public List<ProductRecommendation> getRecommendationsForUser(Long userId) {
        // Vérifier le cache
        if (recommendationCache.containsKey(userId)) {
            return recommendationCache.get(userId);
        }

        // Obtenir les recommandations du modèle
        List<ProductRecommendation> recommendations = recommender.recommendForUser(userId);

        // Mettre en cache
        if (!recommendations.isEmpty()) {
            recommendationCache.put(userId, recommendations);
        }

        return recommendations;
    }

    /**
     * Obtient les produits similaires à un produit donné
     * ("Les clients ayant acheté X ont aussi acheté Y")
     */
    public List<ProductRecommendation> getSimilarProducts(Long productId) {
        return recommender.recommendSimilarProducts(productId);
    }

    /**
     * Obtient les produits les plus populaires
     * Basé sur le nombre d'interactions utilisateur
     */
    public List<ProductRecommendation> getPopularProducts() {
        return recommender.getPopularRecommendations();
    }

    /**
     * Obtient les recommandations basées sur le panier actuel
     * Analyse les produits fréquemment achetés ensemble
     */
    public List<ProductRecommendation> getCartBasedRecommendations(List<Long> cartProductIds) {
        if (cartProductIds == null || cartProductIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Agréger les recommandations pour tous les produits du panier
        java.util.Map<Long, Double> aggregatedScores = new java.util.HashMap<>();
        java.util.Map<Long, String> reasons = new java.util.HashMap<>();

        for (Long productId : cartProductIds) {
            List<ProductRecommendation> similar = recommender.recommendSimilarProducts(productId);
            
            for (ProductRecommendation rec : similar) {
                if (cartProductIds.contains(rec.productId())) {
                    continue; // Ne pas recommander un produit déjà dans le panier
                }
                
                aggregatedScores.merge(rec.productId(), rec.score(), Double::sum);
                reasons.putIfAbsent(rec.productId(), 
                    "Complémentaire à votre panier");
            }
        }

        // Construire la liste finale
        List<ProductRecommendation> recommendations = new ArrayList<>();
        for (java.util.Map.Entry<Long, Double> entry : aggregatedScores.entrySet()) {
            recommendations.add(new ProductRecommendation(
                entry.getKey(),
                entry.getValue(),
                reasons.getOrDefault(entry.getKey(), "Recommandation personnalisée")
            ));
        }

        // Trier et limiter
        recommendations.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        if (recommendations.size() > 10) {
            recommendations = recommendations.subList(0, 10);
        }

        return recommendations;
    }

    /**
     * Enregistre une interaction utilisateur-produit
     * À appeler lors de chaque action utilisateur
     */
    @Transactional
    public void recordInteraction(Long userId, Long productId, InteractionType type) {
        // Cette méthode serait appelée par les contrôleurs lors des actions utilisateur
        // Pour l'instant, nous nous basons sur les données existantes
        log.debug("Interaction enregistrée: user={}, product={}, type={}", 
                  userId, productId, type);
    }

    /**
     * Obtient les statistiques du système de recommandation
     */
    public RecommendationStats getRecommendationStats() {
        RecommenderStats modelStats = recommender.getStats();
        
        return new RecommendationStats(
            modelStats.isTrained(),
            modelStats.numUsers(),
            modelStats.numProducts(),
            modelStats.totalInteractions(),
            recommendationCache.size(),
            lastCacheUpdate
        );
    }

    /**
     * Génère une explication personnalisée pour une recommandation
     */
    public String generateExplanation(Long userId, Long productId) {
        // Trouver les utilisateurs similaires qui ont acheté ce produit
        // Cette méthode pourrait être enrichie avec plus de contexte
        return "Recommandé basé sur vos préférences et l'activité des utilisateurs similaires";
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private List<UserProductInteraction> collectInteractions() {
        List<UserProductInteraction> interactions = new ArrayList<>();

        // 1. Récupérer les comportements utilisateur (views, etc.)
        List<UserBehavior> behaviors = userBehaviorRepository.findAll();
        
        for (UserBehavior behavior : behaviors) {
            if (behavior.getUserId() != null && behavior.getProduitId() != null) {
                InteractionType type = mapBehaviorType(behavior.getType());
                interactions.add(new UserProductInteraction(
                    behavior.getUserId(),
                    behavior.getProduitId(),
                    type
                ));
            }
        }

        // 2. Récupérer les achats (lignes de commande)
        List<LigneCommande> lignes = ligneCommandeRepository.findAll();
        
        for (LigneCommande ligne : lignes) {
            if (ligne.getCommande() != null && ligne.getProduit() != null) {
                Long userId = extractUserIdFromCommande(ligne);
                if (userId != null) {
                    interactions.add(new UserProductInteraction(
                        userId,
                        ligne.getProduit().getProduitId(),
                        InteractionType.PURCHASE
                    ));
                }
            }
        }

        return interactions;
    }

    private InteractionType mapBehaviorType(String type) {
        if (type == null) return InteractionType.VIEW;
        
        return switch (type.toUpperCase()) {
            case "VIEW", "PAGE_VIEW" -> InteractionType.VIEW;
            case "ADD_TO_CART", "CART" -> InteractionType.CART;
            case "PURCHASE", "BUY" -> InteractionType.PURCHASE;
            case "WISHLIST", "WISH" -> InteractionType.WISHLIST;
            default -> InteractionType.VIEW;
        };
    }

    private Long extractUserIdFromCommande(LigneCommande ligne) {
        // À adapter selon votre structure de données
        // Si vous avez un lien commande -> utilisateur
        if (ligne.getCommande() != null && ligne.getCommande().getCommandeId() != null) {
            // Pour l'instant, on utilise l'ID de commande comme proxy
            // En production, il faudrait récupérer l'ID utilisateur réel
            return ligne.getCommande().getCommandeId(); // Placeholder
        }
        return null;
    }

    // ==================== CLASSES DE DONNÉES ====================

    public record RecommendationStats(
        boolean modelTrained,
        int totalUsers,
        int totalProducts,
        int totalInteractions,
        int cachedRecommendations,
        LocalDateTime lastUpdate
    ) {}
}
