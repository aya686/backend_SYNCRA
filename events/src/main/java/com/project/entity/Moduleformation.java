package com.project.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "moduleformation")
public class Moduleformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Long moduleId;

    private String titre;
    private Integer ordre;
    private Integer duree;
    private String contenu;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    @JsonIgnore
    private Formation formation;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Contenu> contenus;

    // Getters et Setters...
    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
    public List<Contenu> getContenus() { return contenus; }
    public void setContenus(List<Contenu> contenus) { this.contenus = contenus; }
}