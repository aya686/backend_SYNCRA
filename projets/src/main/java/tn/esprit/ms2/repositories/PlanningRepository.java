package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Planning;
import java.util.List;
import java.util.Optional;

public interface PlanningRepository extends JpaRepository<Planning, Long> {
    List<Planning> findByProjetId(Long projetId);
    Optional<Planning> findBySprintId(Long sprintId);
}