package com.example.projetpi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "programme_prevention")
public class ProgrammePrevention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;
    private String nom;
    private String objectif;
    private Integer dureesemaines;
    private Double progression;
    private Boolean actif;

    public ProgrammePrevention() {}

    public ProgrammePrevention(Long id, Long utilisateurId, String nom, String objectif, Integer dureesemaines, Double progression, Boolean actif) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.objectif = objectif;
        this.dureesemaines = dureesemaines;
        this.progression = progression;
        this.actif = actif;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public String getNom() { return nom; }
    public String getObjectif() { return objectif; }
    public Integer getDureesemaines() { return dureesemaines; }
    public Double getProgression() { return progression; }
    public Boolean getActif() { return actif; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public void setNom(String nom) { this.nom = nom; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public void setDureesemaines(Integer dureesemaines) { this.dureesemaines = dureesemaines; }
    public void setProgression(Double progression) { this.progression = progression; }
    public void setActif(Boolean actif) { this.actif = actif; }
}