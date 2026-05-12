package com.synchub.ms6.repository;

import com.synchub.ms6.entity.PricingConfiguration;
import com.synchub.ms6.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingConfigurationRepository extends JpaRepository<PricingConfiguration, Long> {

    List<PricingConfiguration> findByProduitProduitId(Long produitId);
    
    Optional<PricingConfiguration> findFirstByProduitProduitIdOrderByDateCreationDesc(Long produitId);

    List<PricingConfiguration> findByStrategie(PricingConfiguration.StrategiePricing strategie);

    List<PricingConfiguration> findByActifTrue();

    @Query("SELECT pc FROM PricingConfiguration pc WHERE pc.actif = true AND pc.elasticitePrix IS NULL")
    List<PricingConfiguration> findActiveWithoutElasticity();

    @Query("SELECT pc FROM PricingConfiguration pc WHERE pc.produit.boutique.boutiqueId = :boutiqueId AND pc.actif = true")
    List<PricingConfiguration> findByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT COUNT(pc) FROM PricingConfiguration pc WHERE pc.strategie = :strategie AND pc.actif = true")
    Long countByStrategie(@Param("strategie") PricingConfiguration.StrategiePricing strategie);

    @Query("SELECT pc.produit, pc.elasticitePrix FROM PricingConfiguration pc WHERE pc.elasticitePrix IS NOT NULL ORDER BY pc.elasticitePrix ASC")
    List<Object[]> findProductsByElasticityOrder();
}
