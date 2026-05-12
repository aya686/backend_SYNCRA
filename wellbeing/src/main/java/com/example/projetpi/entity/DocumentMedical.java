package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class DocumentMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String type;
    private String url;

    @ManyToOne
    @JoinColumn(name = "dossier_sante_id")
    @JsonBackReference
    private DossierSante dossierSante;

    // Constructeurs
    public DocumentMedical() {}

    public DocumentMedical(String nom, String type, String url) {
        this.nom = nom;
        this.type = type;
        this.url = url;
    }

    // Getters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public DossierSante getDossierSante() { return dossierSante; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setType(String type) { this.type = type; }
    public void setUrl(String url) { this.url = url; }
    public void setDossierSante(DossierSante dossierSante) { this.dossierSante = dossierSante; }
}