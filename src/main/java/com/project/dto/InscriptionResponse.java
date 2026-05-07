package com.project.dto;

import com.project.entity.Inscription;

import java.time.LocalDateTime;

public class InscriptionResponse {
    private Long inscriptionId;
    private Long evenementId;
    private Long participantId;
    private String statut;
    private Boolean present;
    private LocalDateTime dateInscription;

    public InscriptionResponse(Inscription inscription) {
        this.inscriptionId = inscription.getInscriptionId();
        this.evenementId = inscription.getEvent() != null ? inscription.getEvent().getEvenementId() : null;
        this.participantId = inscription.getParticipant() != null ? inscription.getParticipant().getParticipantId() : null;
        this.statut = inscription.getStatut();
        this.present = inscription.getPresent();
        this.dateInscription = inscription.getDateInscription();
    }

    // Getters
    public Long getInscriptionId() { return inscriptionId; }
    public Long getEvenementId() { return evenementId; }
    public Long getParticipantId() { return participantId; }
    public String getStatut() { return statut; }
    public Boolean getPresent() { return present; }
    public LocalDateTime getDateInscription() { return dateInscription; }
}