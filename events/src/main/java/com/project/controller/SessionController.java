package com.project.controller;

import java.util.List;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.entity.Session;
import com.project.entity.Formation;
import com.project.dto.SessionRequest;
import com.project.dto.SessionResponse;
import com.project.service.SessionService;
import com.project.service.FormationService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private FormationService formationService;

    @GetMapping
    public List<SessionResponse> getAll(@RequestParam(required = false) Long formationId) {
        List<Session> sessions;
        if (formationId != null) {
            sessions = sessionService.getByFormationId(formationId);
        } else {
            sessions = sessionService.getAll();
        }
        return sessions.stream()
                .map(SessionResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @PostMapping
    public SessionResponse create(@RequestBody SessionRequest request) {
        System.out.println("=== REQUÊTE REÇUE ===");
        System.out.println("Formation ID: " + request.getFormationId());
        System.out.println("Lieu: " + request.getLieu());
        System.out.println("Formateur: " + request.getFormateur());
        System.out.println("Date début: " + request.getDateDebut());
        System.out.println("Date fin: " + request.getDateFin());
        System.out.println("Capacité: " + request.getCapaciteMax());
        System.out.println("Statut: " + request.getStatut());

        Session session = new Session();

        // Remplir les champs
        session.setTitre(request.getTitre() != null ? request.getTitre() : "Session de formation");
        session.setLieu(request.getLieu());
        session.setIntervenant(request.getFormateur());
        session.setDateHeure(request.getDateDebut());
        session.setDateFin(request.getDateFin());
        session.setCapaciteMax(request.getCapaciteMax());
        session.setStatut(request.getStatut() != null ? request.getStatut() : "planifiee");

        // Calculer la durée en heures avec Duration
        if (request.getDateDebut() != null && request.getDateFin() != null) {
            Duration duration = Duration.between(request.getDateDebut(), request.getDateFin());
            long diffHours = duration.toHours();
            session.setDuree((int) diffHours);
        } else {
            session.setDuree(0);
        }

        // Récupérer et lier la formation
        Formation formation = formationService.getById(request.getFormationId());
        session.setFormation(formation);

        // Sauvegarder
        Session saved = sessionService.save(session);
        System.out.println("Session sauvegardée avec ID: " + saved.getSessionId());

        return new SessionResponse(saved);
    }

    @GetMapping("/{id}")
    public SessionResponse getById(@PathVariable Long id) {
        Session session = sessionService.getById(id);
        return new SessionResponse(session);
    }

    @PutMapping("/{id}")
    public SessionResponse update(@PathVariable Long id, @RequestBody SessionRequest request) {
        Session session = sessionService.getById(id);

        session.setTitre(request.getTitre() != null ? request.getTitre() : session.getTitre());
        session.setLieu(request.getLieu());
        session.setIntervenant(request.getFormateur());
        session.setDateHeure(request.getDateDebut());
        session.setDateFin(request.getDateFin());
        session.setCapaciteMax(request.getCapaciteMax());
        session.setStatut(request.getStatut());

        if (request.getDateDebut() != null && request.getDateFin() != null) {
            Duration duration = Duration.between(request.getDateDebut(), request.getDateFin());
            long diffHours = duration.toHours();
            session.setDuree((int) diffHours);
        }

        Session updated = sessionService.save(session);
        return new SessionResponse(updated);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        sessionService.delete(id);
        return "Session supprimée avec succès !";
    }

}