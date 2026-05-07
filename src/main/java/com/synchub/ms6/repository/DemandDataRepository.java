package com.synchub.ms6.repository;

import com.synchub.ms6.entity.DemandData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandDataRepository extends JpaRepository<DemandData, Long> {

    List<DemandData> findByProduitProduitIdOrderByDatePeriodeDesc(Long produitId);

    @Query("SELECT dd FROM DemandData dd WHERE dd.produit.produitId = :produitId AND dd.datePeriode >= :since ORDER BY dd.datePeriode DESC")
    List<DemandData> findRecentDemandData(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT dd FROM DemandData dd WHERE dd.produit.produitId = :produitId ORDER BY dd.datePeriode DESC LIMIT 2")
    List<DemandData> findLastTwoPeriods(@Param("produitId") Long produitId);

    Optional<DemandData> findTopByProduitProduitIdOrderByDatePeriodeDesc(Long produitId);

    @Query("SELECT AVG(dd.quantiteVendue) FROM DemandData dd WHERE dd.produit.produitId = :produitId AND dd.datePeriode >= :since")
    Double calculateAverageQuantitySold(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(dd.prixApplique) FROM DemandData dd WHERE dd.produit.produitId = :produitId AND dd.datePeriode >= :since")
    Double calculateAveragePriceApplied(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT dd.prixApplique, SUM(dd.quantiteVendue) FROM DemandData dd WHERE dd.produit.produitId = :produitId AND dd.datePeriode >= :since GROUP BY dd.prixApplique ORDER BY dd.prixApplique")
    List<Object[]> getPriceQuantityCorrelation(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(dd.tauxConversion) FROM DemandData dd WHERE dd.produit.produitId = :produitId AND dd.datePeriode >= :since")
    Double calculateAverageConversionRate(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);
}
