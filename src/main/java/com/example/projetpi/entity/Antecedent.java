package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Antecedent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;

    @ManyToOne
    @JoinColumn(name = "dossier_sante_id")
    @JsonBackReference
    private DossierSante dossierSante;

    // Constructeurs
    public Antecedent() {}

    public Antecedent(String type, String description) {
        this.type = type;
        this.description = description;
    }

    // Getters
    public Long getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public DossierSante getDossierSante() { return dossierSante; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setDossierSante(DossierSante dossierSante) { this.dossierSante = dossierSante; }
}