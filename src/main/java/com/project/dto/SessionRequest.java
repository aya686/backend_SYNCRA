package com.project.dto;

import java.time.LocalDateTime;

public class SessionRequest {
    private Long formationId;
    private String lieu;
    private String formateur;
    private String titre;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Integer capaciteMax;
    private String statut;

    // Constructeurs
    public SessionRequest() {}

    public SessionRequest(Long formationId, String lieu, String formateur,
                          String titre, LocalDateTime dateDebut, LocalDateTime dateFin,
                          Integer capaciteMax, String statut) {
        this.formationId = formationId;
        this.lieu = lieu;
        this.formateur = formateur;
        this.titre = titre;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capaciteMax = capaciteMax;
        this.statut = statut;
    }

    // Getters et Setters
    public Long getFormationId() { return formationId; }
    public void setFormationId(Long formationId) { this.formationId = formationId; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getFormateur() { return formateur; }
    public void setFormateur(String formateur) { this.formateur = formateur; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}