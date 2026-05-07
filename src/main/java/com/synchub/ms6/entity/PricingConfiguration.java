package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "prix_base", nullable = false)
    private Double prixBase;

    @Column(name = "prix_min")
    private Double prixMin;

    @Column(name = "prix_max")
    private Double prixMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategie", nullable = false)
    @Builder.Default
    private StrategiePricing strategie = StrategiePricing.DYNAMIQUE;

    @Column(name = "elasticite_prix")
    private Double elasticitePrix;

    @Column(name = "coefficient_demande")
    @Builder.Default
    private Double coefficientDemande = 1.0;

    @Column(name = "coefficient_concurrence")
    @Builder.Default
    private Double coefficientConcurrence = 1.0;

    @Column(name = "coefficient_saisonnalite")
    @Builder.Default
    private Double coefficientSaisonnalite = 1.0;

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (prixMin == null) prixMin = prixBase * 0.7;
        if (prixMax == null) prixMax = prixBase * 1.3;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum StrategiePricing {
        STATIQUE,           // Prix fixe
        DYNAMIQUE,          // Ajustement automatique
        PENETRATION,        // Prix bas pour gain marché
        SKIMMING,           // Prix haut puis descente
        COMPETITIVE,        // Suivi concurrence
        ELASTICITE_BASEE    // Basé sur courbe de demande
    }

    // Méthode utilitaire pour vérifier si un prix est dans les limites
    public boolean isPrixValide(Double prix) {
        return prix != null && prix >= prixMin && prix <= prixMax;
    }

    // Calculer la marge de manœuvre en pourcentage
    public Double getMargeManoeuvre() {
        return ((prixMax - prixMin) / prixBase) * 100;
    }
}
