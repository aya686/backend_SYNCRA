package com.project.dto;

import java.time.LocalDateTime;

public class EventDTO {
    private Long evenementId;
    private String titre;
    private String type;
    private String lieu;
    private LocalDateTime dateHeure;
    private LocalDateTime dateFin;  // ← AJOUTER
    private Integer capacite;
    private Double prix;
    private String statut;
    private Double latitude;  // ← AJOUTER
    private Double longitude;  // ← AJOUTER
    private String imageUrl;
    private String description;

    public EventDTO() {}

    public EventDTO(Long evenementId, String titre, String type, String lieu, LocalDateTime dateHeure, LocalDateTime dateFin, Integer capacite, Double prix, String statut, Double latitude, Double longitude, String imageUrl) {
        this.evenementId = evenementId;
        this.titre = titre;
        this.type = type;
        this.lieu = lieu;
        this.dateHeure = dateHeure;
        this.dateFin = dateFin;
        this.capacite = capacite;
        this.prix = prix;
        this.statut = statut;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;

    }

    // Getters et Setters
    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public LocalDateTime getDateFin() { return dateFin; }  // ← AJOUTER
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }  // ← AJOUTER

    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) { this.capacite = capacite; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Double getLatitude() { return latitude; }  // ← AJOUTER
    public void setLatitude(Double latitude) { this.latitude = latitude; }  // ← AJOUTER

    public Double getLongitude() { return longitude; }  // ← AJOUTER
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }// ← AJOUTER

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}