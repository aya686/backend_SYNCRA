package com.project.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificat")
public class Certificat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificat_id")
    private Long certificatId;

    @Column(name = "dateemission")
    private LocalDateTime dateEmission;

    @Column(name = "codeverification")
    private String codeVerification;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    @JsonIgnore
    private Formation formation;

    // Getters et Setters...
    public Long getCertificatId() { return certificatId; }
    public void setCertificatId(Long certificatId) { this.certificatId = certificatId; }
    public LocalDateTime getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDateTime dateEmission) { this.dateEmission = dateEmission; }
    public String getCodeVerification() { return codeVerification; }
    public void setCodeVerification(String codeVerification) { this.codeVerification = codeVerification; }
    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
}