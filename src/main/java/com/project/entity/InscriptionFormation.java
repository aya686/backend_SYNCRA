// com/project/entity/InscriptionFormation.java
package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscription_formation")
public class InscriptionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inscription_formation_id")
    private Long inscriptionFormationId;

    @ManyToOne
    @JoinColumn(name = "participant_formation_id", nullable = false)
    private ParticipantFormation participantFormation;

    @Column(name = "formation_id", nullable = false)
    private Long formationId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @Column(name = "statut")
    private String statut;

    @Column(name = "progression")
    private Integer progression;

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;

    // Getters et Setters
    public Long getInscriptionFormationId() { return inscriptionFormationId; }
    public void setInscriptionFormationId(Long inscriptionFormationId) { this.inscriptionFormationId = inscriptionFormationId; }

    public ParticipantFormation getParticipantFormation() { return participantFormation; }
    public void setParticipantFormation(ParticipantFormation participantFormation) { this.participantFormation = participantFormation; }

    public Long getFormationId() { return formationId; }
    public void setFormationId(Long formationId) { this.formationId = formationId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getProgression() { return progression; }
    public void setProgression(Integer progression) { this.progression = progression; }

    public LocalDateTime getDateCompletion() { return dateCompletion; }
    public void setDateCompletion(LocalDateTime dateCompletion) { this.dateCompletion = dateCompletion; }
}