package com.project.dto;

import java.time.LocalDateTime;

public class InscriptionRequest {
    private Long participantId;
    private Long evenementId;
    private String statut;
    private Boolean present;
    private LocalDateTime dateInscription;

    // NOUVEAUX CHAMPS POUR L'EMAIL
    private String email;
    private String nomParticipant;
    private String eventTitle;
    private String eventDate;
    private String eventLieu;

    public InscriptionRequest() {}

    // Getters et Setters existants
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Boolean getPresent() { return present; }
    public void setPresent(Boolean present) { this.present = present; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    // NOUVEAUX GETTERS/SETTERS
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