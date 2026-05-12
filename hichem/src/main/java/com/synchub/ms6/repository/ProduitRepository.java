package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByActifTrue();
    List<Produit> findByBoutiqueBoutiqueId(Long boutiqueId);
    List<Produit> findByBoutiqueBoutiqueIdAndActifTrue(Long boutiqueId);
    List<Produit> findByCategoriesContaining(String category);
    List<Produit> findByPromotionsPromoId(Long promoId);

    @Modifying
    @Query(value = "DELETE FROM produit_promotion WHERE produit_id = :produitId", nativeQuery = true)
    void deleteProduitPromotionRelations(@Param("produitId") Long produitId);

    @Modifying
    @Query(value = "DELETE FROM user_behaviors WHERE produit_id = :produitId", nativeQuery = true)
    void deleteUserBehaviors(@Param("produitId") Long produitId);

    @Modifying
    @Query(value = "DELETE FROM recommendation_logs WHERE produit_recommended_id = :produitId OR produit_source_id = :produitId", nativeQuery = true)
    void deleteRecommendationLogs(@Param("produitId") Long produitId);

    @Modifying
    @Query(value = "DELETE FROM alertes_stock WHERE produit_id = :produitId", nativeQuery = true)
    void deleteAlertesStock(@Param("produitId") Long produitId);
    
    /**
     * Chercher des produits par nom (contient, insensible à la casse)
     */
    List<Produit> findByNomContainingIgnoreCase(String nom);
    
    /**
     * Chercher un produit par nom exact (insensible à la casse)
     */
    Optional<Produit> findByNomIgnoreCase(String nom);
}
