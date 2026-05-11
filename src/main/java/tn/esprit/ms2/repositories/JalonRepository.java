package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Jalon;
import java.util.List;

public interface JalonRepository extends JpaRepository<Jalon, Long> {
    List<Jalon> findByProjetId(Long projetId);
    List<Jalon> findByProjetIdAndAtteint(Long projetId, Boolean atteint);
}