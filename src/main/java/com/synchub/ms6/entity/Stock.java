package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false, unique = true)
    private Produit produit;

    @Builder.Default
    private Integer quantite = 0;

    @Column(name = "seuilalerte")
    @Builder.Default
    private Integer seuilAlerte = 10;

    private String entrepot;

    public boolean isAlerteStock() {
        return quantite <= seuilAlerte;
    }

    public boolean isStockEpuise() {
        return quantite == 0;
    }

    public boolean isStockSuffisant(int quantiteDemandee) {
        return quantite >= quantiteDemandee;
    }
}
