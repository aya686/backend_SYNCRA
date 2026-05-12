package com.synchub.ms6.repository;

import com.synchub.ms6.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProduitProduitIdOrderByDateChangementDesc(Long produitId);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.produit.produitId = :produitId AND ph.dateChangement >= :since ORDER BY ph.dateChangement DESC")
    List<PriceHistory> findRecentChanges(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(ph.variationPercent) FROM PriceHistory ph WHERE ph.produit.produitId = :produitId AND ph.dateChangement >= :since")
    Double calculateAverageVariation(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.raison = :raison ORDER BY ph.dateChangement DESC")
    List<PriceHistory> findByReason(@Param("raison") PriceHistory.RaisonChangement raison);

    @Query("SELECT ph.produit.produitId, COUNT(ph), AVG(ph.variationPercent) FROM PriceHistory ph WHERE ph.dateChangement >= :since GROUP BY ph.produit.produitId")
    List<Object[]> getPriceChangeStats(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(ph.revenuPeriode) FROM PriceHistory ph WHERE ph.dateChangement BETWEEN :debut AND :fin")
    Double calculateTotalRevenueImpact(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
