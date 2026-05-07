// com/project/entity/ParticipantFormation.java
package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participant_formation")
public class ParticipantFormation {

    @Id
    @Column(name = "participant_formation_id")
    private Long participantFormationId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "message")
    private String message;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    // Getters et Setters
    public Long getParticipantFormationId() { return participantFormationId; }
    public void setParticipantFormationId(Long participantFormationId) { this.participantFormationId = participantFormationId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}