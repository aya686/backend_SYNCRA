package com.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "programme")
public class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "programme_id")
    private Long programmeId;

    private Integer ordre;
    private String sujet;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    private String intervenant;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Event event;

    public Programme() {}

    // Getters et Setters
    public Long getProgrammeId() { return programmeId; }
    public void setProgrammeId(Long programmeId) { this.programmeId = programmeId; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getIntervenant() { return intervenant; }
    public void setIntervenant(String intervenant) { this.intervenant = intervenant; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}