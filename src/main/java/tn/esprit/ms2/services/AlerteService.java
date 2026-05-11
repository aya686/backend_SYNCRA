package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms2.entities.*;
import tn.esprit.ms2.repositories.AlerteRepository;
import tn.esprit.ms2.repositories.ProjetRepository;
import tn.esprit.ms2.repositories.TacheRepository;
import tn.esprit.ms2.DTO.AnalyseIAResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final GroqService groqService;
    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Alerte> getAll() { return alerteRepository.findAll(); }

    public Alerte getById(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + id));
    }

    public Alerte create(Alerte alerte) { return alerteRepository.save(alerte); }

    public Alerte createAndPublish(Alerte alerte) {
        Alerte saved = alerteRepository.save(alerte);
        System.out.println("[ALERTE → MS7] type=" + saved.getType()
                + " | utilisateur=" + saved.getUtilisateurId()
                + " | projet=" + saved.getProjetId()
                + " | msg=" + saved.getMessage());
        return saved;
    }

    public Alerte marquerTraitee(Long id) {
        Alerte a = getById(id);
        a.setTraitee(true);
        return alerteRepository.save(a);
    }

    public Alerte update(Long id, Alerte updated) {
        Alerte a = getById(id);
        a.setType(updated.getType());
        a.setMessage(updated.getMessage());
        a.setTraitee(updated.getTraitee());
        return alerteRepository.save(a);
    }

    public void delete(Long id) { alerteRepository.deleteById(id); }

    public List<Alerte> getByUtilisateur(Long utilisateurId) {
        return alerteRepository.findByUtilisateurId(utilisateurId);
    }
    public List<Alerte> getByProjet(Long projetId) {
        return alerteRepository.findByProjetId(projetId);
    }
    public List<Alerte> getNonTraitees() {
        return alerteRepository.findByTraitee(false);
    }
    public List<Alerte> getByType(TypeAlerte type) {
        return alerteRepository.findByType(type);
    }
    public List<Alerte> getNonTraiteesByUtilisateur(Long utilisateurId) {
        return alerteRepository.findByUtilisateurIdAndTraitee(utilisateurId, false);
    }
    public List<Alerte> getNonTraitees(Long utilisateurId) {
        return alerteRepository.findByUtilisateurIdAndTraitee(utilisateurId, false);
    }
    public List<Alerte> getNonTraiteesParUtilisateur(Long utilisateurId) {
        return alerteRepository.findByUtilisateurIdAndTraitee(utilisateurId, false);
    }

    public Alerte getAlerteBloquante(Long utilisateurId) {
        return alerteRepository
                .findByUtilisateurIdAndTraitee(utilisateurId, false)
                .stream()
                .filter(a -> a.getType() == TypeAlerte.SURCHARGE
                        || a.getType() == TypeAlerte.SURCHARGE)
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public AnalyseIAResponse analyserAvecIA(Long alerteId) {

        // 1. Alerte EN PREMIER
        Alerte alerte = getById(alerteId);

        // 2. Tâches en retard
        List<Tache> tachesEnRetard = tacheRepository.findAll().stream()
                .filter(t -> t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDate.now())
                        && t.getStatut() != StatutTache.TERMINE)
                .collect(Collectors.toList());

        // 3. Alertes actives
        List<Alerte> alertesActives = alerteRepository
                .findByUtilisateurIdAndTraitee(alerte.getUtilisateurId(), false);

        // 4. Métriques
        int nbTachesEnRetard = tachesEnRetard.size();
        int niveauCharge     = Math.min(10, 4 + nbTachesEnRetard);
        int nbHeuresSemaine  = 40;
        int nbAlertesActives = alertesActives.size();

        // 5. Titre projet — DANS LA MÉTHODE
        String titreProjet = alerte.getProjetId() != null
                ? projetRepository.findById(alerte.getProjetId())
                .map(Projet::getTitre)
                .orElse("projet inconnu")
                : "aucun projet spécifique";

        // 6. Appel Groq
        String jsonReponse = groqService.analyserAlerte(
                alerte.getType().name(),
                alerte.getMessage(),
                niveauCharge,
                nbTachesEnRetard,
                nbHeuresSemaine,
                nbAlertesActives,
                titreProjet
        );

        // 7. Parse
        try {
            String cleaned = jsonReponse.trim()
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();
            AnalyseIAResponse response = objectMapper.readValue(cleaned, AnalyseIAResponse.class);
            response.setTypeAlerte(alerte.getType().name());
            return response;
        } catch (Exception e) {
            System.err.println("[PARSE ERROR] " + e.getMessage() + " | raw: " + jsonReponse);
            return new AnalyseIAResponse(
                    "Analyse basée sur " + nbTachesEnRetard
                            + " tâche(s) en retard sur : " + titreProjet,
                    niveauCharge,
                    List.of(
                            "Priorisez vos " + nbTachesEnRetard + " tâches en retard",
                            "Réduisez votre charge sur le projet " + titreProjet,
                            "Consultez votre moniteur pour un suivi personnalisé"
                    ),
                    niveauCharge >= 8,
                    alerte.getType().name()
            );
        }
    }
}