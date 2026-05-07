package com.project.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "formateur")
public class Formateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formateur_id")
    private Long formateurId;

    @Column(name = "nom")  // ← Vérifie que cette annotation existe
    private String nom;

    @Column(name = "prenom")  // ← Vérifie que cette annotation existe
    private String prenom;

    @Column(name = "expertise")
    private String expertise;

    @Column(name = "bio")
    private String bio;

    @Column(name = "notemoyenne")
    private Double noteMoyenne;

    @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL)
    private List<Session> sessions;

    // Getters et Setters
    public Long getFormateurId() { return formateurId; }
    public void setFormateurId(Long formateurId) { this.formateurId = formateurId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }
}