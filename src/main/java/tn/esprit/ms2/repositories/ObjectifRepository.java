package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Objectif;
import java.util.List;

public interface ObjectifRepository extends JpaRepository<Objectif, Long> {
    List<Objectif> findByProjetId(Long projetId);
    List<Objectif> findByProjetIdAndAtteint(Long projetId, Boolean atteint);
}