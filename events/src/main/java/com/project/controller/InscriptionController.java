package com.project.controller;

import com.project.dto.InscriptionResponse;
import com.project.entity.Inscription;
import com.project.entity.Event;
import com.project.entity.Participant;
import com.project.repository.InscriptionRepository;
import com.project.repository.EventRepository;
import com.project.repository.ParticipantRepository;
import com.project.service.InscriptionService;
import com.project.service.EmailService;
import com.project.service.PdfTicketService;  // ← AJOUTER CET IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/inscriptions")
public class InscriptionController {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private InscriptionService inscriptionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfTicketService pdfTicketService;  // ← AJOUTER CETTE LIGNE

    // GET - Récupérer toutes les inscriptions
    @GetMapping
    public List<Inscription> getAll() {
        return inscriptionRepository.findAll();
    }

    // GET - Récupérer une inscription par ID
    @GetMapping("/{id}")
    public ResponseEntity<Inscription> getById(@PathVariable Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        return ResponseEntity.ok(inscription);
    }

    // GET - Récupérer les inscriptions d'un événement
    @GetMapping("/event/{eventId}")
    public List<Inscription> getByEventId(@PathVariable Long eventId) {
        return inscriptionRepository.findByEvent_EvenementId(eventId);
    }

    // Compter les inscrits pour un événement
    @GetMapping("/{id}/inscrits")
    public ResponseEntity<Long> getNbInscrits(@PathVariable Long id) {
        long count = inscriptionService.getNbInscritsByEventId(id);
        return ResponseEntity.ok(count);
    }

    // POST - Créer une inscription
    @PostMapping
    public ResponseEntity<?> create(@RequestBody InscriptionRequest request) {
        try {
            System.out.println("=== CRÉATION INSCRIPTION ===");
            System.out.println("Evenement ID: " + request.getEvenementId());
            System.out.println("Participant ID: " + request.getParticipantId());
            System.out.println("Email: " + request.getEmail());

            Event event = eventRepository.findById(request.getEvenementId())
                    .orElseThrow(() -> new RuntimeException("Événement non trouvé avec ID: " + request.getEvenementId()));

            Participant participant = participantRepository.findById(request.getParticipantId())
                    .orElseThrow(() -> new RuntimeException("Participant non trouvé avec ID: " + request.getParticipantId()));

            Inscription inscription = new Inscription();
            inscription.setEvent(event);
            inscription.setParticipant(participant);
            inscription.setStatut(request.getStatut() != null ? request.getStatut() : "confirme");
            inscription.setPresent(request.getPresent() != null ? request.getPresent() : false);
            inscription.setDateInscription(LocalDateTime.now());

            Inscription saved = inscriptionRepository.save(inscription);
            System.out.println("Inscription créée avec ID: " + saved.getInscriptionId());

            // Envoyer l'email avec le ticket PDF
            try {
                double prix = event.getPrix() != null ? event.getPrix() : 0;
                emailService.sendConfirmationEmailWithPdf(
                        request.getEmail(),
                        request.getNomParticipant(),
                        event.getTitre(),
                        event.getDateHeure() != null ?
                                event.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "Date à confirmer",
                        event.getLieu(),
                        saved.getInscriptionId(),
                        prix
                );
                System.out.println("Email avec ticket PDF envoyé à: " + request.getEmail());
            } catch (Exception e) {
                System.err.println("Erreur envoi email: " + e.getMessage());
            }

            InscriptionResponse response = new InscriptionResponse(saved);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PUT - Mettre à jour le statut de présence
    @PutMapping("/{id}/presence")
    public ResponseEntity<Inscription> updatePresence(@PathVariable Long id, @RequestParam Boolean present) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        inscription.setPresent(present);
        Inscription updated = inscriptionRepository.save(inscription);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Supprimer une inscription
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET - Télécharger le ticket PDF
    // Dans la méthode downloadTicket
    // Dans la méthode downloadTicket
    // GET - Télécharger le ticket PDF
    @GetMapping("/ticket/{inscriptionId}")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long inscriptionId) {
        try {
            Inscription inscription = inscriptionRepository.findById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

            String nom = inscription.getParticipant().getNom();
            String email = inscription.getParticipant().getEmail();
            String eventTitle = inscription.getEvent().getTitre();
            String eventDate = inscription.getEvent().getDateHeure() != null ?
                    inscription.getEvent().getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "Date à confirmer";
            String eventLieu = inscription.getEvent().getLieu();
            double prix = inscription.getEvent().getPrix() != null ? inscription.getEvent().getPrix() : 0;

            byte[] pdfBytes = pdfTicketService.generateTicketBytes(nom, email, eventTitle, eventDate, eventLieu, inscriptionId, prix);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=ticket_" + inscriptionId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // Classe DTO pour la requête
    public static class InscriptionRequest {
        private Long evenementId;
        private Long participantId;
        private String statut;
        private Boolean present;
        private String dateInscription;
        private String email;
        private String nomParticipant;
        private String eventTitle;
        private String eventDate;
        private String eventLieu;

        // Getters et Setters
        public Long getEvenementId() { return evenementId; }
        public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }
        public Long getParticipantId() { return participantId; }
        public void setParticipantId(Long participantId) { this.participantId = participantId; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
        public Boolean getPresent() { return present; }
        public void setPresent(Boolean present) { this.present = present; }
        public String getDateInscription() { return dateInscription; }
        public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNomParticipant() { return nomParticipant; }
        public void setNomParticipant(String nomParticipant) { this.nomParticipant = nomParticipant; }
        public String getEventTitle() { return eventTitle; }
        public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
        public String getEventDate() { return eventDate; }
        public void setEventDate(String eventDate) { this.eventDate = eventDate; }
        public String getEventLieu() { return eventLieu; }
        public void setEventLieu(String eventLieu) { this.eventLieu = eventLieu; }
    }
}