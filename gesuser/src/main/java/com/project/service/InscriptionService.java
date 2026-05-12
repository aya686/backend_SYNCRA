package com.project.service;

import com.project.dto.InscriptionRequest;
import com.project.entity.Event;
import com.project.entity.Inscription;
import com.project.entity.Participant;
import com.project.repository.EventRepository;
import com.project.repository.InscriptionRepository;
import com.project.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscriptionService {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    // Récupérer toutes les inscriptions
    public List<Inscription> getAll() {
        return inscriptionRepository.findAll();
    }

    // Récupérer une inscription par ID
    public Inscription getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée avec id: " + id));
    }

    // Récupérer les inscriptions d'un événement
    public List<Inscription> getByEventId(Long eventId) {
        return inscriptionRepository.findByEvent_EvenementId(eventId);
    }

    // Récupérer les inscriptions d'un participant
    public List<Inscription> getByParticipantId(Long participantId) {
        return inscriptionRepository.findByParticipant_ParticipantId(participantId);
    }

    // Compter le nombre d'inscrits pour un événement
    public long getNbInscritsByEventId(Long eventId) {
        return inscriptionRepository.countByEvent_EvenementId(eventId);
    }

    // Créer une inscription
    public Inscription createInscription(InscriptionRequest request) {
        Event event = eventRepository.findById(request.getEvenementId())
                .orElseThrow(() -> new RuntimeException("Événement non trouvé avec id: " + request.getEvenementId()));

        Participant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant non trouvé avec id: " + request.getParticipantId()));

        Inscription inscription = new Inscription();
        inscription.setEvent(event);
        inscription.setParticipant(participant);
        inscription.setStatut(request.getStatut() != null ? request.getStatut() : "confirme");
        inscription.setPresent(request.getPresent() != null ? request.getPresent() : false);
        inscription.setDateInscription(request.getDateInscription() != null ?
                request.getDateInscription() : LocalDateTime.now());

        return inscriptionRepository.save(inscription);
    }

    // Mettre à jour la présence
    public Inscription updatePresence(Long id, Boolean present) {
        Inscription inscription = getById(id);
        inscription.setPresent(present);
        return inscriptionRepository.save(inscription);
    }

    // Supprimer une inscription
    public void deleteInscription(Long id) {
        inscriptionRepository.deleteById(id);
    }

    // Dans InscriptionService.java

    public double getTauxPopularite(Long eventId) {
        // Récupérer l'événement
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        // Compter les inscriptions validées (statut = 'confirme')
        long nbInscrits = inscriptionRepository.countByEvent_EvenementIdAndStatut(eventId, "confirme");

        // Calculer le taux de popularité
        if (event.getCapacite() == null || event.getCapacite() == 0) {
            return 0.0;
        }

        return (double) nbInscrits / event.getCapacite();
    }

}