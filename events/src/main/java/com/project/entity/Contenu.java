package com.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contenu")
public class Contenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contenu_id")
    private Long contenuId;

    private String type;
    private String url;
    private String description;
    private Integer duree;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Moduleformation module;

    public Contenu() {}

    // Getters et Setters
    public Long getContenuId() { return contenuId; }
    public void setContenuId(Long contenuId) { this.contenuId = contenuId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }

    public Moduleformation getModule() { return module; }
    public void setModule(Moduleformation module) { this.module = module; }
}