package com.example.projetpi.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String specialite;
    private String telephone;

    // 🔥 AJOUTE CE CHAMP
    private String adresseCabinet;

    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consultation> consultations = new ArrayList<>();

    // Constructeurs
    public Medecin() {}

    public Medecin(String nom, String prenom, String specialite, String telephone, String adresseCabinet) {
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.telephone = telephone;
        this.adresseCabinet = adresseCabinet;
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getSpecialite() { return specialite; }
    public String getTelephone() { return telephone; }
    public String getAdresseCabinet() { return adresseCabinet; }  // ← AJOUTE CE GETTER
    public List<Consultation> getConsultations() { return consultations; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setAdresseCabinet(String adresseCabinet) { this.adresseCabinet = adresseCabinet; }  // ← AJOUTE CE SETTER
    public void setConsultations(List<Consultation> consultations) { this.consultations = consultations; }
}