package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rappel")
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rappel_id")
    private Long rappelId;

    @Column(name = "dateenvoi")
    private LocalDateTime dateEnvoi;

    private String canal;
    private String message;
    private Boolean envoye;

    @OneToOne
    @JoinColumn(name = "inscription_id")
    private Inscription inscription;

    public Rappel() {}

    // Getters et Setters
    public Long getRappelId() { return rappelId; }
    public void setRappelId(Long rappelId) { this.rappelId = rappelId; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getEnvoye() { return envoye; }
    public void setEnvoye(Boolean envoye) { this.envoye = envoye; }

    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }
}