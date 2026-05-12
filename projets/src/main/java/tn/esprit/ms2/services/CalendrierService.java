package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Calendrier;
import tn.esprit.ms2.entities.Tache;
import tn.esprit.ms2.entities.TypeCalendrier;
import tn.esprit.ms2.repositories.CalendrierRepository;
import tn.esprit.ms2.repositories.TacheRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendrierService {

    private final CalendrierRepository calendrierRepository;
    private final TacheRepository tacheRepository;

    public List<Calendrier> getAll() { return calendrierRepository.findAll(); }

    public Calendrier getById(Long id) {
        return calendrierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrée calendrier non trouvée: " + id));
    }

    public Calendrier create(Calendrier calendrier) {
        if (calendrier.getTache() != null && calendrier.getTache().getId() != null) {
            Tache tache = tacheRepository.findById(calendrier.getTache().getId())
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
            calendrier.setTache(tache);
        }
        return calendrierRepository.save(calendrier);
    }

    public Calendrier update(Long id, Calendrier updated) {
        Calendrier c = getById(id);
        c.setDateDebut(updated.getDateDebut());
        c.setDateFin(updated.getDateFin());
        c.setType(updated.getType());
        c.setEvenementId(updated.getEvenementId());
        return calendrierRepository.save(c);
    }

    public void delete(Long id) { calendrierRepository.deleteById(id); }
    public List<Calendrier> getByProjet(Long projetId) { return calendrierRepository.findByProjetId(projetId); }
    public List<Calendrier> getByType(TypeCalendrier type) { return calendrierRepository.findByType(type); }
}