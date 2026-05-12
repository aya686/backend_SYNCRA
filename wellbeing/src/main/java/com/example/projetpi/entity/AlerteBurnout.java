package com.example.projetpi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "alerte_burnout")
public class AlerteBurnout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;
    private String niveauRisque;
    private String declencheur;
    private LocalDate date;
    private Boolean traitee;
    private String source;

    public AlerteBurnout() {}

    public AlerteBurnout(Long id, Long utilisateurId, String niveauRisque, String declencheur, LocalDate date, Boolean traitee, String source) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.niveauRisque = niveauRisque;
        this.declencheur = declencheur;
        this.date = date;
        this.traitee = traitee;
        this.source = source;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public String getNiveauRisque() { return niveauRisque; }
    public String getDeclencheur() { return declencheur; }
    public LocalDate getDate() { return date; }
    public Boolean getTraitee() { return traitee; }
    public String getSource() { return source; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public void setNiveauRisque(String niveauRisque) { this.niveauRisque = niveauRisque; }
    public void setDeclencheur(String declencheur) { this.declencheur = declencheur; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTraitee(Boolean traitee) { this.traitee = traitee; }
    public void setSource(String source) { this.source = source; }
}