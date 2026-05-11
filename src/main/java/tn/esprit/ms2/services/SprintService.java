package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.entities.Sprint;
import tn.esprit.ms2.entities.StatutSprint;
import tn.esprit.ms2.repositories.ProjetRepository;
import tn.esprit.ms2.repositories.SprintRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjetRepository projetRepository;

    public List<Sprint> getAll() { return sprintRepository.findAll(); }

    public Sprint getById(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint non trouvé: " + id));
    }

    public Sprint create(Long projetId, Sprint sprint) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        sprint.setProjet(projet);
        return sprintRepository.save(sprint);
    }

    public Sprint update(Long id, Sprint updated) {
        Sprint s = getById(id);
        s.setNom(updated.getNom());
        s.setDateDebut(updated.getDateDebut());
        s.setDateFin(updated.getDateFin());
        s.setChargeTotal(updated.getChargeTotal());
        s.setStatut(updated.getStatut());
        return sprintRepository.save(s);
    }

    public void delete(Long id) { sprintRepository.deleteById(id); }
    public List<Sprint> getByProjet(Long projetId) { return sprintRepository.findByProjetId(projetId); }
    public List<Sprint> getByStatut(StatutSprint statut) { return sprintRepository.findByStatut(statut); }
}