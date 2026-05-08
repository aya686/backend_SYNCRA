package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.PlanLimite;
import java.util.List;
import java.util.Optional;

public interface PlanLimiteRepository extends JpaRepository<PlanLimite, Long> {
    List<PlanLimite> findByPlanId(Long planId);
    Optional<PlanLimite> findByPlanIdAndCle(Long planId, String cle);
}