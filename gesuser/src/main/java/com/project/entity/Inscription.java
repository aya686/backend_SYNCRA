package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscription")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inscription_id")
    private Long inscriptionId;

    @Column(name = "dateinscription")
    private LocalDateTime dateInscription;

    private String statut;
    private Boolean present;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    // Constructeurs
    public Inscription() {}

    // Getters et Setters
    public Long getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(Long inscriptionId) { this.inscriptionId = inscriptionId; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Boolean getPresent() { return present; }
    public void setPresent(Boolean present) { this.present = present; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }
}