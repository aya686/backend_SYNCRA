package com.synchub.ms6.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une route de livraison optimisée calculée par l'algorithme VRP.
 * Contient la séquence de points de livraison ordonnée pour minimiser la distance totale.
 */
@Entity
@Table(name = "routes_livraison")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @Column(nullable = false)
    private String nom;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_optimisation")
    private LocalDateTime dateOptimisation;

    @Convert(converter = com.synchub.ms6.converter.StatutRouteConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private StatutRoute statut = StatutRoute.PLANIFIEE;

    @Column(name = "distance_totale_km")
    private Double distanceTotaleKm;

    @Column(name = "duree_estimee_minutes")
    private Integer dureeEstimeeMinutes;

    @Column(name = "cout_estime")
    private Double coutEstime;

    @Column(name = "capacite_vehicule")
    private Integer capaciteVehicule;

    @Column(name = "poids_total_kg")
    private Double poidsTotalKg;

    @Column(name = "volume_total_m3")
    private Double volumeTotalM3;

    @Column(name = "point_depart")
    private String pointDepart;

    @Column(name = "latitude_depart")
    private Double latitudeDepart;

    @Column(name = "longitude_depart")
    private Double longitudeDepart;

    @Column(name = "transporteur")
    private String transporteur;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("ordre ASC")
    private List<PointLivraison> points = new ArrayList<>();

    @Column(name = "nb_points")
    private Integer nbPoints;

    @Column(name = "nb_colis")
    private Integer nbColis;

    @Column(name = "fenetre_debut")
    private LocalDateTime fenetreDebut;

    @Column(name = "fenetre_fin")
    private LocalDateTime fenetreFin;

    @Column(name = "contrainte_temps_max")
    private Integer contrainteTempsMax;

    @Column(name = "algorithme_utilise")
    private String algorithmeUtilise;

    @Column(name = "iteration_optimisation")
    private Integer iterationOptimisation;

    @Column(name = "amelioration_2opt_percent")
    private Double amelioration2OptPercent;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    /**
     * Calcule les statistiques de la route basées sur les points
     */
    public void calculerStatistiques() {
        if (points == null || points.isEmpty()) {
            this.nbPoints = 0;
            this.nbColis = 0;
            this.poidsTotalKg = 0.0;
            this.volumeTotalM3 = 0.0;
            return;
        }

        this.nbPoints = points.size();
        this.nbColis = points.stream()
                .mapToInt(PointLivraison::getNbColis)
                .sum();
        this.poidsTotalKg = points.stream()
                .mapToDouble(PointLivraison::getPoidsKg)
                .sum();
        this.volumeTotalM3 = points.stream()
                .mapToDouble(PointLivraison::getVolumeM3)
                .sum();
    }

    /**
     * Vérifie si la route respecte les contraintes de capacité
     */
    public boolean respecteContraintesCapacite() {
        if (capaciteVehicule == null) return true;
        return nbColis != null && nbColis <= capaciteVehicule;
    }

    /**
     * Calcule la densité de livraison (points par km)
     */
    public Double calculerDensite() {
        if (distanceTotaleKm == null || distanceTotaleKm == 0 || nbPoints == null) {
            return 0.0;
        }
        return nbPoints / distanceTotaleKm;
    }

    public enum StatutRoute {
        PLANIFIEE,      // Route créée mais non assignée
        ASSIGNNEE,      // Assignée à un transporteur
        EN_COURS,       // Livraisons en cours
        TERMINEE,       // Toutes livraisons effectuées
        ANNULEE         // Route annulée
    }
}
