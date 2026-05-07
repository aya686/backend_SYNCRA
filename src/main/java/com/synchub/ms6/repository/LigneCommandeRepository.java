package com.synchub.ms6.repository;

import com.synchub.ms6.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {
    List<LigneCommande> findByCommandeCommandeId(Long commandeId);
    List<LigneCommande> findByProduitProduitId(Long produitId);
}
