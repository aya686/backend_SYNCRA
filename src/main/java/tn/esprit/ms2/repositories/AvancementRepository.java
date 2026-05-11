package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Avancement;
import java.util.Optional;

public interface AvancementRepository extends JpaRepository<Avancement, Long> {
    Optional<Avancement> findByProjetId(Long projetId);
}