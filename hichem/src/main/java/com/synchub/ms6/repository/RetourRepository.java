package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Retour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetourRepository extends JpaRepository<Retour, Long> {
    List<Retour> findByLivraisonLivraisonId(Long livraisonId);
    List<Retour> findByStatut(Retour.StatutRetour statut);
}
