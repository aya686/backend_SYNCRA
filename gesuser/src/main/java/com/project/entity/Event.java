package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "evenement")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evenement_id")
    private Long evenementId;

    @Column(name = "titre")
    private String titre;

    @Column(name = "type")
    private String type;

    @Column(name = "lieu")
    private String lieu;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "dateheure")
    private LocalDateTime dateHeure;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "datefin")  // ← AJOUTER CETTE LIGNE
    private LocalDateTime dateFin;  // ← AJOUTER CETTE LIGNE

    @Column(name = "capacite")
    private Integer capacite;

    @Column(name = "prix")
    private Double prix;

    @Column(name = "statut")
    private String statut;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "image_url", length = 500)  // ← AJOUTER
    private String imageUrl;

    @Column(name = "description", length = 1000)  // ← NOUVEAU CHAMP
    private String description;


    // Constructeur par défaut
    public Event() {}

    // Getters et Setters
    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Event{" +
                "evenementId=" + evenementId +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", lieu='" + lieu + '\'' +
                ", dateHeure=" + dateHeure +
                ", dateFin=" + dateFin +
                ", capacite=" + capacite +
                ", prix=" + prix +
                ", statut='" + statut + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}