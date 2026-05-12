package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Affectation;
import java.util.List;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findByTacheId(Long tacheId);
    List<Affectation> findByUtilisateurId(Long utilisateurId);
}