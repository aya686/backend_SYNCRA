package com.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lieu")
public class Lieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lieu_id")
    private Long lieuId;

    private String nom;
    private String adresse;
    private Integer capacite;
    private String ville;

    public Lieu() {}

    // Getters et Setters
    public Long getLieuId() { return lieuId; }
    public void setLieuId(Long lieuId) { this.lieuId = lieuId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) { this.capacite = capacite; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
}