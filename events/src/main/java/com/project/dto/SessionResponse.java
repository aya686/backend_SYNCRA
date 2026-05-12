package com.project.dto;

import java.time.LocalDateTime;
import com.project.entity.Session;

public class SessionResponse {
    private Long sessionId;
    private String titre;
    private String lieu;
    private String intervenant;
    private LocalDateTime dateHeure;
    private LocalDateTime dateFin;
    private Integer capaciteMax;
    private String statut;
    private Long formationId;

    public SessionResponse(Session session) {
        this.sessionId = session.getSessionId();
        this.titre = session.getTitre();
        this.lieu = session.getLieu();
        this.intervenant = session.getIntervenant();
        this.dateHeure = session.getDateHeure();
        this.dateFin = session.getDateFin();
        this.capaciteMax = session.getCapaciteMax();
        this.statut = session.getStatut();
        this.formationId = session.getFormation() != null ? session.getFormation().getFormationId() : null;
    }

    // Getters
    public Long getSessionId() { return sessionId; }
    public String getTitre() { return titre; }
    public String getLieu() { return lieu; }
    public String getIntervenant() { return intervenant; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public LocalDateTime getDateFin() { return dateFin; }
    public Integer getCapaciteMax() { return capaciteMax; }
    public String getStatut() { return statut; }
    public Long getFormationId() { return formationId; }
}