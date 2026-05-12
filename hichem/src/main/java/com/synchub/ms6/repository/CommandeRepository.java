package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByStatut(Commande.StatutCommande statut);
    List<Commande> findByDateBetween(LocalDateTime start, LocalDateTime end);
    List<Commande> findByStatutAndDateBetween(Commande.StatutCommande statut, LocalDateTime start, LocalDateTime end);
    
    /**
     * Chercher les commandes par ID utilisateur
     */
    List<Commande> findByUserId(Long userId);
    
    /**
     * Chercher les commandes par ID utilisateur triées par date décroissante
     */
    List<Commande> findByUserIdOrderByDateDesc(Long userId);
}
