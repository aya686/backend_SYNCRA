package com.synchub.ms6.repository;

import com.synchub.ms6.entity.AlerteStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlerteStockRepository extends JpaRepository<AlerteStock, Long> {

    List<AlerteStock> findByProduitProduitId(Long produitId);

    List<AlerteStock> findByType(AlerteStock.TypeAlerte type);

    List<AlerteStock> findByEmailEnvoye(Boolean emailEnvoye);

    @Query("SELECT a FROM AlerteStock a WHERE a.dateAlerte BETWEEN :debut AND :fin")
    List<AlerteStock> findByDateBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT a FROM AlerteStock a WHERE a.produit.produitId = :produitId AND a.type = :type AND a.dateAlerte >= :dateMin")
    List<AlerteStock> findRecentByProduitAndType(
            @Param("produitId") Long produitId,
            @Param("type") AlerteStock.TypeAlerte type,
            @Param("dateMin") LocalDateTime dateMin);

    @Query("SELECT COUNT(a) FROM AlerteStock a WHERE a.emailEnvoye = false")
    Long countAlertesNonEnvoyees();
}
