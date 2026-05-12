package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodePromo(String codePromo);

    @Query("SELECT p FROM Promotion p WHERE p.dateDebut <= :now AND p.dateFin >= :now")
    List<Promotion> findActivePromotions(LocalDateTime now);

    // findAll() is inherited from JpaRepository - returns all promotions

    boolean existsByCodePromo(String codePromo);

    @Modifying
    @Query(value = "DELETE FROM produit_promotion WHERE promo_id = :promoId", nativeQuery = true)
    void deleteProduitPromotionRelations(@Param("promoId") Long promoId);
}
