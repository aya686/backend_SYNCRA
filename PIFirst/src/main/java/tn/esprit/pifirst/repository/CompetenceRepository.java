package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Competence;
import java.util.List;

public interface CompetenceRepository extends JpaRepository<Competence, Long> {
    // AJOUTER CETTE LIGNE
    List<Competence> findByUserId(Long idUser);
}