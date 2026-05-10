package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "article_sante")
public class ArticleSante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    private String auteur;
    private String contenu;
    private LocalDate datePublication;
    private String tags;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ressource_bienetre_id")
    private RessourceBienetre ressourceBienetre;

    public ArticleSante() {}

    public ArticleSante(Long articleId, String auteur, String contenu, LocalDate datePublication, String tags, RessourceBienetre ressourceBienetre) {
        this.articleId = articleId;
        this.auteur = auteur;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.tags = tags;
        this.ressourceBienetre = ressourceBienetre;
    }

    // Getters
    public Long getArticleId() { return articleId; }
    public String getAuteur() { return auteur; }
    public String getContenu() { return contenu; }
    public LocalDate getDatePublication() { return datePublication; }
    public String getTags() { return tags; }
    public RessourceBienetre getRessourceBienetre() { return ressourceBienetre; }

    // Setters
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }
    public void setTags(String tags) { this.tags = tags; }
    public void setRessourceBienetre(RessourceBienetre ressourceBienetre) { this.ressourceBienetre = ressourceBienetre; }
}