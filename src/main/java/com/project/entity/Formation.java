package com.project.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "formation")
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formation_id")
    private Long formationId;

    private String titre;
    private String niveau;

    @Column(name = "duréeheures")
    private Integer dureeHeures;

    private Boolean certificate;
    private Double prix;
    private String statut;

    @Column(name = "description", length = 1000)  // ← NOUVEAU CHAMP
    private String description;

    // Relation ManyToOne avec Formateur
    @ManyToOne
    @JoinColumn(name = "formateur_id")
    private Formateur formateur;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Moduleformation> modules;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Session> sessions;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Certificat> certificats;

    // Getters et Setters
    public Long getFormationId() { return formationId; }
    public void setFormationId(Long formationId) { this.formationId = formationId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public Integer getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(Integer dureeHeures) { this.dureeHeures = dureeHeures; }
    public Boolean getCertificate() { return certificate; }
    public void setCertificate(Boolean certificate) { this.certificate = certificate; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Formateur getFormateur() { return formateur; }
    public void setFormateur(Formateur formateur) { this.formateur = formateur; }
    public List<Moduleformation> getModules() { return modules; }
    public void setModules(List<Moduleformation> modules) { this.modules = modules; }
    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }
    public List<Certificat> getCertificats() { return certificats; }
    public void setCertificats(List<Certificat> certificats) { this.certificats = certificats; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}