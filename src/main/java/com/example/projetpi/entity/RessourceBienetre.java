package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ressource_bienetre")
public class RessourceBienetre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ressourceId;

    private String titre;
    private String type;
    private String description;
    private String url;
    private Boolean gratuit;
    private String niveau;

    @JsonIgnore
    @OneToMany(mappedBy = "ressourceBienetre", cascade = CascadeType.ALL)
    private List<Exercice> exercices;

    @JsonIgnore
    @OneToMany(mappedBy = "ressourceBienetre", cascade = CascadeType.ALL)
    private List<ArticleSante> articlesSante;

    public RessourceBienetre() {}

    public RessourceBienetre(Long ressourceId, String titre, String type, String description, String url, Boolean gratuit, String niveau, List<Exercice> exercices, List<ArticleSante> articlesSante) {
        this.ressourceId = ressourceId;
        this.titre = titre;
        this.type = type;
        this.description = description;
        this.url = url;
        this.gratuit = gratuit;
        this.niveau = niveau;
        this.exercices = exercices;
        this.articlesSante = articlesSante;
    }

    // Getters
    public Long getRessourceId() { return ressourceId; }
    public String getTitre() { return titre; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public Boolean getGratuit() { return gratuit; }
    public String getNiveau() { return niveau; }
    public List<Exercice> getExercices() { return exercices; }
    public List<ArticleSante> getArticlesSante() { return articlesSante; }

    // Setters
    public void setRessourceId(Long ressourceId) { this.ressourceId = ressourceId; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setUrl(String url) { this.url = url; }
    public void setGratuit(Boolean gratuit) { this.gratuit = gratuit; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setExercices(List<Exercice> exercices) { this.exercices = exercices; }
    public void setArticlesSante(List<ArticleSante> articlesSante) { this.articlesSante = articlesSante; }
}