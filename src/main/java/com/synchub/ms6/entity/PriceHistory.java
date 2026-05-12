package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "prix_avant", nullable = false)
    private Double prixAvant;

    @Column(name = "prix_apres", nullable = false)
    private Double prixApres;

    @Column(name = "variation_percent")
    private Double variationPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "raison_changement")
    private RaisonChangement raison;

    @Column(name = "facteur_declencheur")
    private String facteurDeclencheur;

    @Column(name = "quantite_vendue_periode")
    private Integer quantiteVenduePeriode;

    @Column(name = "revenu_periode")
    private Double revenuPeriode;

    @Column(name = "elasticite_calculee")
    private Double elasticiteCalculee;

    @Column(name = "date_changement")
    private LocalDateTime dateChangement;

    @PrePersist
    protected void onCreate() {
        dateChangement = LocalDateTime.now();
        if (variationPercent == null && prixAvant != null && prixAvant > 0) {
            variationPercent = ((prixApres - prixAvant) / prixAvant) * 100;
        }
    }

    public enum RaisonChangement {
        AJUSTEMENT_DEMANDE,
        VARIATION_STOCK,
        CONCURRENCE,
        SAISONNALITE,
        PROMOTION,
        ELASTICITE_OPTIMISEE,
        MANUEL,
        SYSTEME
    }

    // Calculer l'impact sur le revenu
    public Double calculerImpactRevenu() {
        if (quantiteVenduePeriode == null || revenuPeriode == null) return null;
        
        Double revenuEstimeAncienPrix = quantiteVenduePeriode * prixAvant;
        return revenuPeriode - revenuEstimeAncienPrix;
    }
}
