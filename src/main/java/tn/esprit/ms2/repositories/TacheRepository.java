package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Tache;
import tn.esprit.ms2.entities.PrioriteTache;
import tn.esprit.ms2.entities.StatutTache;
import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByProjetId(Long projetId);
    List<Tache> findByStatut(StatutTache statut);
    List<Tache> findByPriorite(PrioriteTache priorite);
    List<Tache> findByProjetIdAndStatut(Long projetId, StatutTache statut);
}