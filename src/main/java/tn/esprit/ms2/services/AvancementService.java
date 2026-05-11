package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Avancement;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.repositories.AvancementRepository;
import tn.esprit.ms2.repositories.ProjetRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvancementService {

    private final AvancementRepository avancementRepository;
    private final ProjetRepository projetRepository;

    public List<Avancement> getAll() { return avancementRepository.findAll(); }

    public Avancement getById(Long id) {
        return avancementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avancement non trouvé: " + id));
    }

    public Avancement getByProjet(Long projetId) {
        return avancementRepository.findByProjetId(projetId)
                .orElseThrow(() -> new RuntimeException("Avancement non trouvé pour projet: " + projetId));
    }

    public Avancement create(Long projetId, Avancement avancement) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        avancement.setProjet(projet);
        return avancementRepository.save(avancement);
    }

    public Avancement update(Long id, Avancement updated) {
        Avancement a = getById(id);
        a.setPourcentage(updated.getPourcentage());
        a.setDateCalcul(updated.getDateCalcul());
        a.setNotesSuivi(updated.getNotesSuivi());
        return avancementRepository.save(a);
    }

    public void delete(Long id) { avancementRepository.deleteById(id); }
}