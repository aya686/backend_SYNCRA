package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.SousTache;
import tn.esprit.ms2.entities.Tache;
import tn.esprit.ms2.repositories.SousTacheRepository;
import tn.esprit.ms2.repositories.TacheRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SousTacheService {

    private final SousTacheRepository sousTacheRepository;
    private final TacheRepository tacheRepository;

    public List<SousTache> getAll() { return sousTacheRepository.findAll(); }

    public SousTache getById(Long id) {
        return sousTacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SousTâche non trouvée: " + id));
    }

    public SousTache create(Long tacheId, SousTache sousTache) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée: " + tacheId));
        sousTache.setTache(tache);
        return sousTacheRepository.save(sousTache);
    }

    public SousTache update(Long id, SousTache updated) {
        SousTache s = getById(id);
        s.setTitre(updated.getTitre());
        s.setStatut(updated.getStatut());
        s.setAssigneId(updated.getAssigneId());
        return sousTacheRepository.save(s);
    }

    public void delete(Long id) { sousTacheRepository.deleteById(id); }
    public List<SousTache> getByTache(Long tacheId) { return sousTacheRepository.findByTacheId(tacheId); }
}