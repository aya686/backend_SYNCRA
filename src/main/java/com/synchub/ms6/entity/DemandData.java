package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "demand_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "demand_id")
    private Long demandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "date_periode")
    private LocalDateTime datePeriode;

    @Column(name = "prix_applique")
    private Double prixApplique;

    @Column(name = "quantite_demandee")
    private Integer quantiteDemandee;

    @Column(name = "quantite_vendue")
    private Integer quantiteVendue;

    @Column(name = "vues_produit")
    private Integer vuesProduit;

    @Column(name = "paniers_ajoutes")
    private Integer paniersAjoutes;

    @Column(name = "taux_conversion")
    private Double tauxConversion;

    @Column(name = "stock_disponible")
    private Integer stockDisponible;

    @Column(name = "periode_type")
    @Enumerated(EnumType.STRING)
    private PeriodeType periodeType;

    @PrePersist
    protected void onCreate() {
        if (datePeriode == null) {
            datePeriode = LocalDateTime.now();
        }
        if (tauxConversion == null && vuesProduit != null && vuesProduit > 0 && paniersAjoutes != null) {
            tauxConversion = (double) paniersAjoutes / vuesProduit * 100;
        }
    }

    public enum PeriodeType {
        HEURE,
        JOUR,
        SEMAINE,
        MOIS
    }

    // Calculer l'élasticité ponctuelle si on a les données comparatives
    public Double calculerElasticitePonctuelle(DemandData periodePrecedente) {
        if (periodePrecedente == null || periodePrecedente.getPrixApplique() == null 
            || periodePrecedente.getQuantiteVendue() == null || periodePrecedente.getQuantiteVendue() == 0
            || this.quantiteVendue == null || this.prixApplique == null) {
            return null;
        }

        double deltaQ = (double) (this.quantiteVendue - periodePrecedente.getQuantiteVendue()) / periodePrecedente.getQuantiteVendue();
        double deltaP = (this.prixApplique - periodePrecedente.getPrixApplique()) / periodePrecedente.getPrixApplique();

        if (deltaP == 0) return null;

        return deltaQ / deltaP;
    }
}
