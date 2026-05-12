package com.project.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "participant")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Column(name = "nom")
    private String nom;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Certificat> certificats;

    // Constructeur par défaut (OBLIGATOIRE)
    public Participant() {}

    // Constructeur avec paramètres
    public Participant(String nom, String email, String role) {
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    // GETTERS ET SETTERS
    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    public void setInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions;
    }

    public List<Certificat> getCertificats() {
        return certificats;
    }

    public void setCertificats(List<Certificat> certificats) {
        this.certificats = certificats;
    }

    // toString pour déboguer
    @Override
    public String toString() {
        return "Participant{" +
                "participantId=" + participantId +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}