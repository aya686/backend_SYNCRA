package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String heure;
    private String motif;

    @ManyToOne
    @JoinColumn(name = "dossier_sante_id")
    @JsonBackReference
    private DossierSante dossierSante;

    // 🔥 AJOUTE CETTE RELATION VERS MEDECIN
    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

    // Constructeurs
    public Consultation() {}

    public Consultation(LocalDate date, String heure, String motif) {
        this.date = date;
        this.heure = heure;
        this.motif = motif;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getHeure() { return heure; }
    public String getMotif() { return motif; }
    public DossierSante getDossierSante() { return dossierSante; }
    public Medecin getMedecin() { return medecin; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeure(String heure) { this.heure = heure; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setDossierSante(DossierSante dossierSante) { this.dossierSante = dossierSante; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }
}