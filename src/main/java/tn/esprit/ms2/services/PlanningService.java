package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Planning;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.entities.Sprint;
import tn.esprit.ms2.repositories.PlanningRepository;
import tn.esprit.ms2.repositories.ProjetRepository;
import tn.esprit.ms2.repositories.SprintRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository planningRepository;
    private final ProjetRepository projetRepository;
    private final SprintRepository sprintRepository;

    public List<Planning> getAll() { return planningRepository.findAll(); }

    public Planning getById(Long id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planning non trouvé: " + id));
    }

    public Planning create(Long projetId, Long sprintId, Planning planning) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint non trouvé: " + sprintId));
        planning.setProjet(projet);
        planning.setSprint(sprint);
        return planningRepository.save(planning);
    }

    public Planning update(Long id, Planning updated) {
        Planning p = getById(id);
        p.setCharge(updated.getCharge());
        p.setCapacite(updated.getCapacite());
        p.setDateGeneration(updated.getDateGeneration());
        return planningRepository.save(p);
    }

    public void delete(Long id) { planningRepository.deleteById(id); }
    public List<Planning> getByProjet(Long projetId) { return planningRepository.findByProjetId(projetId); }
}