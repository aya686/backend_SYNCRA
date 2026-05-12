package tn.esprit.pifirst.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Plan;
import tn.esprit.pifirst.entity.PlanLimite;
import tn.esprit.pifirst.repository.PlanLimiteRepository;
import tn.esprit.pifirst.repository.PlanRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanLimiteRepository planLimiteRepository;

    public List<Plan> getAll() {
        return planRepository.findAll();
    }

    public Plan getById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));
    }

    public Integer getLimiteValue(Long planId, String cle) {
        PlanLimite limite = planLimiteRepository.findByPlanIdAndCle(planId, cle)
                .orElseThrow(() -> new RuntimeException("Limite non trouvée pour ce plan"));
        return limite.getValeur();
    }

    public Plan create(Plan plan) {
        return planRepository.save(plan);
    }

    public Plan update(Long id, Plan updated) {
        Plan existing = getById(id);
        existing.setType(updated.getType());
        existing.setPrix(updated.getPrix());
        existing.setDureeJours(updated.getDureeJours());
        existing.setDescription(updated.getDescription());
        return planRepository.save(existing);
    }

    public void delete(Long id) {
        planRepository.deleteById(id);
    }
}