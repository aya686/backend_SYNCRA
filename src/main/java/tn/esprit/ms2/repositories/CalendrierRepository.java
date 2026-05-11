package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Calendrier;
import tn.esprit.ms2.entities.TypeCalendrier;
import java.util.List;

public interface CalendrierRepository extends JpaRepository<Calendrier, Long> {
    List<Calendrier> findByProjetId(Long projetId);
    List<Calendrier> findByType(TypeCalendrier type);
    List<Calendrier> findByTacheId(Long tacheId);
}