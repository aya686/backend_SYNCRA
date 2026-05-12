package com.project.controller;

import com.project.dto.InscriptionFormationRequest;
import com.project.entity.Formation;
import com.project.entity.InscriptionFormation;
import com.project.entity.ParticipantFormation;
import com.project.entity.Session;
import com.project.repository.InscriptionFormationRepository;
import com.project.repository.FormationRepository;
import com.project.repository.ParticipantFormationRepository;
import com.project.service.InscriptionFormationService;
import com.project.service.EmailService;
import com.project.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/inscriptions-formation")
public class InscriptionFormationController {

    @Autowired
    private InscriptionFormationService service;

    @Autowired
    private InscriptionFormationRepository inscriptionFormationRepository;

    @Autowired
    private FormationRepository formationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ParticipantFormationRepository participantFormationRepository;

    @GetMapping
    public List<InscriptionFormation> getAll() {
        return service.getAll();
    }

    @GetMapping("/participant/{participantId}")
    public List<InscriptionFormation> getByParticipant(@PathVariable Long participantId) {
        return service.getByParticipantId(participantId);
    }

    @GetMapping("/formation/{formationId}")
    public List<InscriptionFormation> getByFormation(@PathVariable Long formationId) {
        return service.getByFormationId(formationId);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkInscription(
            @RequestParam Long participantId,
            @RequestParam Long formationId) {
        boolean isInscribed = service.isAlreadyInscribed(participantId, formationId);
        return ResponseEntity.ok(isInscribed);
    }

    // InscriptionFormationController.java - Modifiez la méthode create()

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InscriptionFormationRequest request) {
        try {
            System.out.println("=== NOUVELLE INSCRIPTION ===");
            System.out.println("Participant ID (compte): " + request.getParticipantId());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Formation ID: " + request.getFormationId());

            // ✅ CRUCIAL: Utiliser le participantId comme ID du participant_formation
            Long participantId = request.getParticipantId();

            // ✅ Vérifier si le participant existe déjà avec cet ID
            Optional<ParticipantFormation> existingParticipantOpt = participantFormationRepository
                    .findById(participantId);

            ParticipantFormation participant;

            if (existingParticipantOpt.isPresent()) {
                // ✅ Réutiliser le participant existant (même ID)
                participant = existingParticipantOpt.get();
                System.out.println("✅ Participant existant trouvé - ID: " + participant.getParticipantFormationId());
            } else {
                // ✅ Créer un nouveau participant avec l'ID du compte
                participant = new ParticipantFormation();
                participant.setParticipantFormationId(participantId); // ← FORCER L'ID !
                participant.setNom(request.getNom());
                participant.setPrenom(request.getPrenom());
                participant.setEmail(request.getEmail());
                participant.setTelephone(request.getTelephone());
                participant.setMessage(request.getMessage());
                participant.setDateCreation(LocalDateTime.now());

                participant = participantFormationRepository.save(participant);
                System.out.println("✅ Nouveau participant créé - ID: " + participant.getParticipantFormationId());
            }



            // Créer l'inscription
            InscriptionFormation inscription = new InscriptionFormation();
            inscription.setParticipantFormation(participant);
            inscription.setFormationId(request.getFormationId());
            inscription.setSessionId(request.getSessionId());
            inscription.setDateInscription(LocalDateTime.now());
            inscription.setStatut("inscrit");
            inscription.setProgression(0);

            InscriptionFormation saved = inscriptionFormationRepository.save(inscription);

            System.out.println("✅ Inscription créée avec participant ID: " + participant.getParticipantFormationId());

            // ... envoyer l'email ...

            return new ResponseEntity<>(saved, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/progression")
    public InscriptionFormation updateProgression(@PathVariable Long id, @RequestParam Integer progression) {
        return service.updateProgression(id, progression);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteInscription(id);
        return "Inscription supprimée avec succès !";
    }

    @Autowired
    private SessionService sessionService;

    @GetMapping("/session/{sessionId}/user/{userId}/check")
    public ResponseEntity<Boolean> checkUserRegistration(
            @PathVariable Long sessionId,
            @PathVariable Long userId) {

        Session session = sessionService.getById(sessionId);
        Long formationId = session.getFormation().getFormationId();

        boolean exists = inscriptionFormationRepository.existsByParticipantIdAndFormationId(userId, formationId);

        return ResponseEntity.ok(exists);
    }

    // InscriptionFormationController.java

    @GetMapping("/formation/{formationId}/participants")
    public ResponseEntity<List<Map<String, Object>>> getParticipantsByFormation(@PathVariable Long formationId) {
        List<InscriptionFormation> inscriptions = inscriptionFormationRepository.findByFormationId(formationId);

        List<Map<String, Object>> participants = new ArrayList<>();

        for (InscriptionFormation insc : inscriptions) {
            Map<String, Object> p = new HashMap<>();
            p.put("inscriptionId", insc.getInscriptionFormationId());
            p.put("dateInscription", insc.getDateInscription());
            p.put("statut", insc.getStatut());
            p.put("progression", insc.getProgression());

            // ✅ VÉRIFICATION CRITIQUE
            ParticipantFormation pf = insc.getParticipantFormation();
            if (pf != null) {
                p.put("participantId", pf.getParticipantFormationId());
                p.put("nom", pf.getNom() != null ? pf.getNom() : "");
                p.put("prenom", pf.getPrenom() != null ? pf.getPrenom() : "");
                p.put("email", pf.getEmail() != null ? pf.getEmail() : "");
                p.put("telephone", pf.getTelephone() != null ? pf.getTelephone() : "");
                p.put("message", pf.getMessage() != null ? pf.getMessage() : "");
            } else {
                // ⚠️ Ne pas ajouter si participant inexistant (ou ajouter avec valeurs par défaut)
                p.put("participantId", null);
                p.put("nom", "Inconnu");
                p.put("prenom", "Inconnu");
                p.put("email", "inconnu@email.com");
                p.put("telephone", "");
                p.put("message", "");
            }

            // ✅ N'AJOUTER QUE SI LE PARTICIPANT EXISTE (optionnel)
            if (pf != null) {
                participants.add(p);
            }
        }

        return ResponseEntity.ok(participants);
    }

}