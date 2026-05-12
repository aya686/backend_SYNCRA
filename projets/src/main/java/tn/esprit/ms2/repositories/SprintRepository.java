package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Sprint;
import tn.esprit.ms2.entities.StatutSprint;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjetId(Long projetId);
    List<Sprint> findByStatut(StatutSprint statut);
}