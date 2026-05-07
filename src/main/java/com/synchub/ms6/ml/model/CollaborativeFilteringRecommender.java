package com.synchub.ms6.ml.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * Système de recommandation collaboratif - User-Based Collaborative Filtering
 * Implémentation from scratch en Java
 * 
 * Algorithme: k-NN collaboratif avec similarité cosinus
 * Recommande des produits basés sur les comportements similaires des utilisateurs
 */
@Component
@Slf4j
public class CollaborativeFilteringRecommender implements Serializable {

    private static final long serialVersionUID = 1L;

    // Matrice utilisateur-produit (sparse)
    private Map<Long, Map<Long, Double>> userProductMatrix; // userId -> (productId -> rating/poids)
    
    // Cache de similarités entre utilisateurs
    private Map<Long, Map<Long, Double>> userSimilarities;
    
    // Statistiques
    private int numUsers;
    private int numProducts;
    private int totalInteractions;

    // Paramètres
    private static final int K_NEIGHBORS = 5; // Nombre de voisins similaires
    private static final double MIN_SIMILARITY = 0.1; // Seuil minimum de similarité
    private static final int MAX_RECOMMENDATIONS = 10;

    public CollaborativeFilteringRecommender() {
        this.userProductMatrix = new HashMap<>();
        this.userSimilarities = new HashMap<>();
    }

    /**
     * Entraîne le modèle sur les données d'interaction utilisateur-produit
     * 
     * Types d'interactions supportés:
     * - VIEW: visualisation (poids 1)
     * - CART: ajout au panier (poids 3)
     * - PURCHASE: achat (poids 5)
     */
    public void train(List<UserProductInteraction> interactions) {
        if (interactions == null || interactions.size() < 20) {
            log.warn("Pas assez d'interactions pour entraîner le modèle (min 20 requis)");
            return;
        }

        log.info("Démarrage de l'entraînement du recommender sur {} interactions...", 
                 interactions.size());

        // Construire la matrice utilisateur-produit
        buildUserProductMatrix(interactions);
        
        // Calculer les similarités entre utilisateurs
        computeUserSimilarities();
        
        // Statistiques
        this.numUsers = userProductMatrix.size();
        this.numProducts = (int) userProductMatrix.values().stream()
                .flatMap(m -> m.keySet().stream())
                .distinct()
                .count();
        this.totalInteractions = interactions.size();

        log.info("Entraînement terminé! {} utilisateurs, {} produits, {} similarités calculées",
                 numUsers, numProducts, userSimilarities.size());
    }

    /**
     * Recommande des produits pour un utilisateur
     * Algorithme: User-Based Collaborative Filtering avec k-NN
     */
    public List<ProductRecommendation> recommendForUser(Long userId) {
        if (!userProductMatrix.containsKey(userId)) {
            log.debug("Utilisateur {} inconnu - retour des recommandations populaires", userId);
            return getPopularRecommendations();
        }

        // Obtenir les voisins similaires
        List<UserSimilarity> neighbors = getKNearestNeighbors(userId);
        
        if (neighbors.isEmpty()) {
            return getPopularRecommendations();
        }

        // Calculer les scores prédits pour les produits non vus
        Map<Long, Double> productScores = new HashMap<>();
        Map<Long, Double> productSimilaritySum = new HashMap<>();
        Set<Long> userProducts = userProductMatrix.get(userId).keySet();

        for (UserSimilarity neighbor : neighbors) {
            Map<Long, Double> neighborProducts = userProductMatrix.get(neighbor.userId);
            
            for (Map.Entry<Long, Double> entry : neighborProducts.entrySet()) {
                Long productId = entry.getKey();
                double neighborRating = entry.getValue();
                
                // Ne pas recommander les produits déjà vus/achetés
                if (userProducts.contains(productId)) continue;
                
                // Score pondéré par la similarité
                double weightedScore = neighborRating * neighbor.similarity;
                
                productScores.merge(productId, weightedScore, Double::sum);
                productSimilaritySum.merge(productId, neighbor.similarity, Double::sum);
            }
        }

        // Normaliser les scores
        List<ProductRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : productScores.entrySet()) {
            Long productId = entry.getKey();
            double score = entry.getValue();
            double simSum = productSimilaritySum.getOrDefault(productId, 1.0);
            
            double normalizedScore = score / simSum;
            
            recommendations.add(new ProductRecommendation(
                productId, 
                normalizedScore,
                generateExplanation(productId, neighbors)
            ));
        }

        // Trier par score décroissant
        recommendations.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        // Limiter le nombre de recommandations
        if (recommendations.size() > MAX_RECOMMENDATIONS) {
            recommendations = recommendations.subList(0, MAX_RECOMMENDATIONS);
        }

        return recommendations;
    }

    /**
     * Recommande des produits similaires à un produit (Item-Based)
     * Basé sur les utilisateurs qui ont acheté les deux produits
     */
    public List<ProductRecommendation> recommendSimilarProducts(Long productId) {
        // Trouver les utilisateurs qui ont acheté ce produit
        Set<Long> productBuyers = new HashSet<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userProductMatrix.entrySet()) {
            if (entry.getValue().containsKey(productId)) {
                productBuyers.add(entry.getKey());
            }
        }

        if (productBuyers.isEmpty()) {
            return new ArrayList<>();
        }

        // Compter les co-occurrences
        Map<Long, Double> coOccurrenceScores = new HashMap<>();
        
        for (Long buyerId : productBuyers) {
            Map<Long, Double> buyerProducts = userProductMatrix.get(buyerId);
            for (Map.Entry<Long, Double> entry : buyerProducts.entrySet()) {
                Long otherProductId = entry.getKey();
                if (!otherProductId.equals(productId)) {
                    double weight = entry.getValue();
                    coOccurrenceScores.merge(otherProductId, weight, Double::sum);
                }
            }
        }

        // Normaliser par le nombre d'acheteurs
        List<ProductRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : coOccurrenceScores.entrySet()) {
            double score = entry.getValue() / productBuyers.size();
            recommendations.add(new ProductRecommendation(
                entry.getKey(), 
                score,
                "Produit fréquemment acheté avec celui-ci"
            ));
        }

        recommendations.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        if (recommendations.size() > MAX_RECOMMENDATIONS) {
            recommendations = recommendations.subList(0, MAX_RECOMMENDATIONS);
        }

        return recommendations;
    }

    /**
     * Calcule la similarité entre deux utilisateurs (Cosine Similarity)
     */
    public double calculateUserSimilarity(Long userId1, Long userId2) {
        Map<Long, Double> products1 = userProductMatrix.get(userId1);
        Map<Long, Double> products2 = userProductMatrix.get(userId2);
        
        if (products1 == null || products2 == null) return 0.0;

        // Trouver les produits communs
        Set<Long> commonProducts = new HashSet<>(products1.keySet());
        commonProducts.retainAll(products2.keySet());
        
        if (commonProducts.isEmpty()) return 0.0;

        // Calculer la similarité cosinus
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long productId : commonProducts) {
            double rating1 = products1.get(productId);
            double rating2 = products2.get(productId);
            dotProduct += rating1 * rating2;
        }

        for (double rating : products1.values()) {
            norm1 += rating * rating;
        }

        for (double rating : products2.values()) {
            norm2 += rating * rating;
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Obtient les statistiques du modèle
     */
    public RecommenderStats getStats() {
        return new RecommenderStats(
            numUsers,
            numProducts,
            totalInteractions,
            !userProductMatrix.isEmpty()
        );
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void buildUserProductMatrix(List<UserProductInteraction> interactions) {
        userProductMatrix.clear();
        
        for (UserProductInteraction interaction : interactions) {
            userProductMatrix
                .computeIfAbsent(interaction.userId(), k -> new HashMap<>())
                .merge(interaction.productId(), interaction.weight(), Double::sum);
        }
    }

    private void computeUserSimilarities() {
        userSimilarities.clear();
        List<Long> userIds = new ArrayList<>(userProductMatrix.keySet());
        
        for (int i = 0; i < userIds.size(); i++) {
            Long userId1 = userIds.get(i);
            
            for (int j = i + 1; j < userIds.size(); j++) {
                Long userId2 = userIds.get(j);
                
                double similarity = calculateUserSimilarity(userId1, userId2);
                
                if (similarity > MIN_SIMILARITY) {
                    userSimilarities
                        .computeIfAbsent(userId1, k -> new HashMap<>())
                        .put(userId2, similarity);
                    userSimilarities
                        .computeIfAbsent(userId2, k -> new HashMap<>())
                        .put(userId1, similarity);
                }
            }
        }
    }

    private List<UserSimilarity> getKNearestNeighbors(Long userId) {
        Map<Long, Double> similarities = userSimilarities.get(userId);
        
        if (similarities == null || similarities.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserSimilarity> neighbors = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : similarities.entrySet()) {
            neighbors.add(new UserSimilarity(entry.getKey(), entry.getValue()));
        }

        // Trier par similarité décroissante
        neighbors.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        
        // Prendre les k plus proches
        if (neighbors.size() > K_NEIGHBORS) {
            neighbors = neighbors.subList(0, K_NEIGHBORS);
        }

        return neighbors;
    }

    public List<ProductRecommendation> getPopularRecommendations() {
        // Compter les interactions par produit
        Map<Long, Double> productPopularity = new HashMap<>();
        
        for (Map<Long, Double> userProducts : userProductMatrix.values()) {
            for (Map.Entry<Long, Double> entry : userProducts.entrySet()) {
                productPopularity.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        List<ProductRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : productPopularity.entrySet()) {
            recommendations.add(new ProductRecommendation(
                entry.getKey(),
                entry.getValue(),
                "Produit populaire"
            ));
        }

        recommendations.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        // Si pas de données, retourner des produits par défaut (pour la démo)
        if (recommendations.isEmpty()) {
            recommendations = getDefaultRecommendations();
        }
        
        if (recommendations.size() > MAX_RECOMMENDATIONS) {
            recommendations = recommendations.subList(0, MAX_RECOMMENDATIONS);
        }

        return recommendations;
    }
    
    /**
     * Retourne des recommandations par défaut quand pas assez de données
     * Utile pour la démonstration et les tests
     */
    private List<ProductRecommendation> getDefaultRecommendations() {
        List<ProductRecommendation> defaults = new ArrayList<>();
        // Produits fictifs populaires pour la démo
        defaults.add(new ProductRecommendation(1L, 5.0, "Produit populaire (recommandation par défaut)"));
        defaults.add(new ProductRecommendation(2L, 4.5, "Produit populaire (recommandation par défaut)"));
        defaults.add(new ProductRecommendation(3L, 4.0, "Produit populaire (recommandation par défaut)"));
        defaults.add(new ProductRecommendation(4L, 3.8, "Produit populaire (recommandation par défaut)"));
        defaults.add(new ProductRecommendation(5L, 3.5, "Produit populaire (recommandation par défaut)"));
        return defaults;
    }

    private String generateExplanation(Long productId, List<UserSimilarity> neighbors) {
        return String.format("Recommandé basé sur %d utilisateurs similaires", neighbors.size());
    }

    // ==================== CLASSES INTERNES ET RECORDS ====================

    public record UserProductInteraction(Long userId, Long productId, 
                                          InteractionType type, double weight) {
        
        public UserProductInteraction(Long userId, Long productId, InteractionType type) {
            this(userId, productId, type, type.defaultWeight);
        }
    }

    public enum InteractionType {
        VIEW(1.0),
        CART(3.0),
        PURCHASE(5.0),
        WISHLIST(2.0);

        final double defaultWeight;

        InteractionType(double weight) {
            this.defaultWeight = weight;
        }
    }

    public record ProductRecommendation(Long productId, double score, String reason) {}

    public record RecommenderStats(int numUsers, int numProducts, 
                                    int totalInteractions, boolean isTrained) {}

    private record UserSimilarity(Long userId, double similarity) {}
}
