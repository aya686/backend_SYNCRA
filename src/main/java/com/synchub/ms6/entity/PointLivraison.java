package com.synchub.ms6.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Représente un point de livraison individuel dans une route optimisée.
 * Fait partie de l'algorithme VRP (Vehicle Routing Problem).
 */
@Entity
@Table(name = "points_livraison")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteLivraison route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livraison_id")
    private Livraison livraison;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @Column(nullable = false)
    private String adresse;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "ville")
    private String ville;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @Column(name = "distance_depart_km")
    private Double distanceDepartKm;

    @Column(name = "distance_precedent_km")
    private Double distancePrecedentKm;

    @Column(name = "distance_cumulee_km")
    private Double distanceCumuleeKm;

    @Column(name = "temps_estime_minutes")
    private Integer tempsEstimeMinutes;

    @Column(name = "temps_cumule_minutes")
    private Integer tempsCumuleMinutes;

    @Column(name = "heure_arrivee_estimee")
    private LocalDateTime heureArriveeEstimee;

    @Column(name = "fenetre_debut")
    private LocalDateTime fenetreDebut;

    @Column(name = "fenetre_fin")
    private LocalDateTime fenetreFin;

    @Column(name = "temps_service_minutes")
    private Integer tempsServiceMinutes;

    @Column(name = "nb_colis")
    @Builder.Default
    private Integer nbColis = 1;

    @Column(name = "poids_kg")
    private Double poidsKg;

    @Column(name = "volume_m3")
    private Double volumeM3;

    @Convert(converter = com.synchub.ms6.converter.StatutPointConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private StatutPoint statut = StatutPoint.PLANIFIE;

    @Column(name = "priorite")
    @Builder.Default
    private Integer priorite = 5;

    @Column(name = "notes")
    private String notes;

    @Column(name = "nom_contact")
    private String nomContact;

    @Column(name = "telephone_contact")
    private String telephoneContact;

    @Column(name = "code_acces")
    private String codeAcces;

    @Column(name = "instructions_speciales")
    private String instructionsSpeciales;

    @Column(name = "temps_attente_estime_minutes")
    private Integer tempsAttenteEstimeMinutes;

    @Column(name = "penalite_fenetre_horaire")
    private Double penaliteFenetreHoraire;

    @Version
    private Long version;

    /**
     * Vérifie si l'arrivée estimée respecte la fenêtre horaire
     */
    public boolean respecteFenetreHoraire() {
        if (heureArriveeEstimee == null) return true;
        if (fenetreDebut != null && heureArriveeEstimee.isBefore(fenetreDebut)) {
            return false;
        }
        if (fenetreFin != null && heureArriveeEstimee.isAfter(fenetreFin)) {
            return false;
        }
        return true;
    }

    /**
     * Calcule la pénalité si la fenêtre horaire n'est pas respectée
     */
    public Double calculerPenalite() {
        if (heureArriveeEstimee == null || penaliteFenetreHoraire == null) return 0.0;
        
        double penalite = 0.0;
        
        if (fenetreDebut != null && heureArriveeEstimee.isBefore(fenetreDebut)) {
            long minutesRetard = java.time.Duration.between(heureArriveeEstimee, fenetreDebut).toMinutes();
            penalite += minutesRetard * penaliteFenetreHoraire;
        }
        
        if (fenetreFin != null && heureArriveeEstimee.isAfter(fenetreFin)) {
            long minutesRetard = java.time.Duration.between(fenetreFin, heureArriveeEstimee).toMinutes();
            penalite += minutesRetard * penaliteFenetreHoraire * 2;
        }
        
        return penalite;
    }

    /**
     * Calcule la distance à vol d'oiseau depuis un autre point (formule de Haversine)
     */
    public Double calculerDistanceDepuis(Double autreLat, Double autreLng) {
        if (latitude == null || longitude == null || autreLat == null || autreLng == null) {
            return null;
        }

        final int R = 6371;

        double latDistance = Math.toRadians(autreLat - latitude);
        double lngDistance = Math.toRadians(autreLng - longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(autreLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public enum StatutPoint {
        PLANIFIE,
        EN_ROUTE,
        ARRIVE,
        LIVRE,
        NON_LIVRE,
        REPORTE
    }
}
