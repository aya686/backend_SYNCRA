package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lignes_commandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ligne_id")
    private Long ligneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prixunitaire", nullable = false)
    private Double prixUnitaire;

    @Column(nullable = false)
    private Double total;

    @PrePersist
    @PreUpdate
    public void calculerTotal() {
        if (quantite != null && prixUnitaire != null) {
            this.total = quantite * prixUnitaire;
        }
    }
}
