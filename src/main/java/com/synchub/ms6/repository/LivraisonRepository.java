package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByCommandeCommandeId(Long commandeId);
    List<Livraison> findByStatut(Livraison.StatutLivraison statut);
}
