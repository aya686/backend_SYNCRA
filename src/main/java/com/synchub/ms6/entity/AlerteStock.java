package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertes_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alerte_id")
    private Long alerteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "quantite_avant", nullable = false)
    private Integer quantiteAvant;

    @Column(name = "quantite_apres", nullable = false)
    private Integer quantiteApres;

    @Column(name = "seuil_alerte", nullable = false)
    private Integer seuilAlerte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Column(name = "email_envoye", nullable = false)
    @Builder.Default
    private Boolean emailEnvoye = false;

    @Column(name = "date_alerte")
    private LocalDateTime dateAlerte;

    @Column(length = 1000)
    private String message;

    @PrePersist
    protected void onCreate() {
        dateAlerte = LocalDateTime.now();
    }

    public enum TypeAlerte {
        STOCK_BAS,      // Stock <= seuil d'alerte
        STOCK_EPUIS,    // Stock = 0
        RESTOCK,        // Réapprovisionnement
        STOCK_INSUFFISANT // Tentative de commande avec stock insuffisant
    }
}
