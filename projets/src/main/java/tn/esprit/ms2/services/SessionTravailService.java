package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms2.DTO.SessionTravailDTO;
import tn.esprit.ms2.entities.Alerte;
import tn.esprit.ms2.entities.SessionTravail;
import tn.esprit.ms2.entities.Tache;
import tn.esprit.ms2.entities.TypeAlerte;
import tn.esprit.ms2.repositories.SessionTravailRepository;
import tn.esprit.ms2.repositories.TacheRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionTravailService {

    private final SessionTravailRepository sessionRepository;
    private final TacheRepository tacheRepository;
    private final AlerteService alerteService;
    private final PlanningIAService planningIAService;

    @Value("${alerte.seuil.charge:8}")
    private int seuilCharge;

    public List<SessionTravail> getAll() { return sessionRepository.findAll(); }

    public SessionTravail getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée: " + id));
    }

    public SessionTravail create(Long tacheId, SessionTravail session) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée: " + tacheId));
        session.setTache(tache);
        SessionTravail saved = sessionRepository.save(session);

        if (session.getNiveauCharge() != null && session.getNiveauCharge() > seuilCharge) {
            Alerte alerte = Alerte.builder()
                    .utilisateurId(session.getUtilisateurId())
                    .projet(tache.getProjet())
                    .type(TypeAlerte.SURCHARGE)
                    .message("Niveau de charge élevé (" + session.getNiveauCharge() + "/10) pour la tâche: " + tache.getTitre())
                    .date(LocalDate.now())
                    .traitee(false)
                    .build();
            alerteService.createAndPublish(alerte);
        }
        return saved;
    }

    public SessionTravail update(Long id, SessionTravail updated) {
        SessionTravail s = getById(id);
        s.setDebut(updated.getDebut());
        s.setFin(updated.getFin());
        s.setDureeMinutes(updated.getDureeMinutes());
        s.setNiveauCharge(updated.getNiveauCharge());
        return sessionRepository.save(s);
    }


    @Transactional
    public SessionTravail create(SessionTravailDTO dto) {
        Tache tache = tacheRepository.findById(dto.getTacheId())
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée: " + dto.getTacheId()));

        SessionTravail session = new SessionTravail();
        session.setTache(tache);
        session.setUtilisateurId(dto.getUtilisateurId());
        session.setNiveauCharge(dto.getNiveauCharge());
        session.setDebut(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    @Transactional
    public SessionTravail terminer(Long id) {
        SessionTravail s = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée: " + id));
        s.setFin(LocalDateTime.now());
        long minutes = java.time.Duration.between(s.getDebut(), s.getFin()).toMinutes();
        s.setDureeMinutes((int) minutes);
        SessionTravail saved = sessionRepository.save(s);
        // Déclenche analyse IA
        planningIAService.analyserProjet(s.getTache().getProjet());
        return saved;
    }
    public void delete(Long id) { sessionRepository.deleteById(id); }
    public List<SessionTravail> getByTache(Long tacheId) { return sessionRepository.findByTacheId(tacheId); }
    public List<SessionTravail> getByUtilisateur(Long utilisateurId) { return sessionRepository.findByUtilisateurId(utilisateurId); }
}