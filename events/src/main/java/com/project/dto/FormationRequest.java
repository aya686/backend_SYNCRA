// com/project/dto/FormationRequest.java
package com.project.dto;

public class FormationRequest {
    private String titre;
    private String description;
    private Integer dureeHeures;
    private String niveau;
    private Double prix;
    private Boolean certificate;
    private String statut;
    private Long formateurId;

    // Constructeurs
    public FormationRequest() {}

    // Getters et Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(Integer dureeHeures) { this.dureeHeures = dureeHeures; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Boolean getCertificate() { return certificate; }
    public void setCertificate(Boolean certificate) { this.certificate = certificate; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getFormateurId() { return formateurId; }
    public void setFormateurId(Long formateurId) { this.formateurId = formateurId; }
}