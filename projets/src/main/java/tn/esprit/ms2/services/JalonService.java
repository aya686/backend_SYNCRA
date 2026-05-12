package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Jalon;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.repositories.JalonRepository;
import tn.esprit.ms2.repositories.ProjetRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JalonService {

    private final JalonRepository jalonRepository;
    private final ProjetRepository projetRepository;

    public List<Jalon> getAll() { return jalonRepository.findAll(); }

    public Jalon getById(Long id) {
        return jalonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jalon non trouvé: " + id));
    }

    public Jalon create(Long projetId, Jalon jalon) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        jalon.setProjet(projet);
        return jalonRepository.save(jalon);
    }

    public Jalon update(Long id, Jalon updated) {
        Jalon j = getById(id);
        j.setTitre(updated.getTitre());
        j.setDateEcheance(updated.getDateEcheance());
        j.setAtteint(updated.getAtteint());
        return jalonRepository.save(j);
    }

    public void delete(Long id) { jalonRepository.deleteById(id); }
    public List<Jalon> getByProjet(Long projetId) { return jalonRepository.findByProjetId(projetId); }
}