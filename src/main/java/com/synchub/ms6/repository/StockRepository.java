package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProduitProduitId(Long produitId);

    @Query("SELECT s FROM Stock s WHERE s.quantite <= s.seuilAlerte")
    List<Stock> findAllStocksEnAlerte();

    @Query("SELECT s FROM Stock s WHERE s.quantite = 0")
    List<Stock> findAllStocksEpuises();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite <= s.seuilAlerte")
    Long countStocksEnAlerte();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite = 0")
    Long countStocksEpuises();

    @Query("SELECT s FROM Stock s WHERE s.produit.boutique.boutiqueId = :boutiqueId AND s.quantite <= s.seuilAlerte")
    List<Stock> findStocksEnAlerteByBoutique(@Param("boutiqueId") Long boutiqueId);
}
