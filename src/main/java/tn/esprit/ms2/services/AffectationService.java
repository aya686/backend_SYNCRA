package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Affectation;
import tn.esprit.ms2.entities.Tache;
import tn.esprit.ms2.repositories.AffectationRepository;
import tn.esprit.ms2.repositories.TacheRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final TacheRepository tacheRepository;

    public List<Affectation> getAll() { return affectationRepository.findAll(); }

    public Affectation getById(Long id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée: " + id));
    }

    public Affectation create(Long tacheId, Affectation affectation) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée: " + tacheId));
        affectation.setTache(tache);
        return affectationRepository.save(affectation);
    }

    public Affectation update(Long id, Affectation updated) {
        Affectation a = getById(id);
        a.setUtilisateurId(updated.getUtilisateurId());
        a.setDateAffectation(updated.getDateAffectation());
        a.setRole(updated.getRole());
        return affectationRepository.save(a);
    }

    public void delete(Long id) { affectationRepository.deleteById(id); }
    public List<Affectation> getByTache(Long tacheId) { return affectationRepository.findByTacheId(tacheId); }
    public List<Affectation> getByUtilisateur(Long utilisateurId) { return affectationRepository.findByUtilisateurId(utilisateurId); }
}