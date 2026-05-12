package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms2.entities.*;
import tn.esprit.ms2.repositories.ProjetRepository;
import tn.esprit.ms2.repositories.SousTacheRepository;
import tn.esprit.ms2.repositories.TacheRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final ProjetService projetService;
    private final SousTacheRepository sousTacheRepository;

    public List<Tache> getAll() { return tacheRepository.findAll(); }

    public Tache getById(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée: " + id));
    }

    public Tache create(Long projetId, Tache tache) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        tache.setProjet(projet);
        return tacheRepository.save(tache);
    }
    @Transactional
    public Tache changerStatut(Long id, StatutTache statut) {
        Tache tache = getById(id);
        tache.setStatut(statut);
        Tache saved = tacheRepository.save(tache);
        // Recalcule avancement automatiquement
        projetService.recalculerAvancement(tache.getProjet().getId());
        return saved;
    }
    public Tache update(Long id, Tache updated) {
        Tache t = getById(id);
        t.setTitre(updated.getTitre());
        t.setDescription(updated.getDescription());
        t.setPriorite(updated.getPriorite());
        t.setStatut(updated.getStatut());
        t.setDeadline(updated.getDeadline());
        t.setEstimationHeures(updated.getEstimationHeures());
        return tacheRepository.save(t);
    }

    public void delete(Long id) { tacheRepository.deleteById(id); }
    public List<SousTache> getSousTaches(Long tacheId) {
        return sousTacheRepository.findByTacheId(tacheId);
    }

    @Transactional
    public SousTache changerStatutSousTache(Long id, StatutSousTache statut) {
        SousTache st = sousTacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SousTache non trouvée: " + id));
        st.setStatut(statut);
        return sousTacheRepository.save(st);

    }

    public List<Tache> getByProjet(Long projetId) { return tacheRepository.findByProjetId(projetId); }
    public List<Tache> getByStatut(StatutTache statut) { return tacheRepository.findByStatut(statut); }
    public List<Tache> getByPriorite(PrioriteTache priorite) { return tacheRepository.findByPriorite(priorite); }
}
