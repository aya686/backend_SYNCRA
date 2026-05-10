package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciceId;

    private String nom;
    private Integer dureeMinutes;
    private String frequence;
    private String instructions;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ressource_bienetre_id")
    private RessourceBienetre ressourceBienetre;

    public Exercice() {}

    public Exercice(Long exerciceId, String nom, Integer dureeMinutes, String frequence, String instructions, RessourceBienetre ressourceBienetre) {
        this.exerciceId = exerciceId;
        this.nom = nom;
        this.dureeMinutes = dureeMinutes;
        this.frequence = frequence;
        this.instructions = instructions;
        this.ressourceBienetre = ressourceBienetre;
    }

    // Getters
    public Long getExerciceId() { return exerciceId; }
    public String getNom() { return nom; }
    public Integer getDureeMinutes() { return dureeMinutes; }
    public String getFrequence() { return frequence; }
    public String getInstructions() { return instructions; }
    public RessourceBienetre getRessourceBienetre() { return ressourceBienetre; }

    // Setters
    public void setExerciceId(Long exerciceId) { this.exerciceId = exerciceId; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setRessourceBienetre(RessourceBienetre ressourceBienetre) { this.ressourceBienetre = ressourceBienetre; }
}