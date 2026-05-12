package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Idee;
import tn.esprit.ms2.entities.StatutIdee;
import java.util.List;

public interface IdeeRepository extends JpaRepository<Idee, Long> {
    List<Idee> findByAuteurId(Long auteurId);
    List<Idee> findByStatut(StatutIdee statut);
    List<Idee> findByProjetIsNull();
}