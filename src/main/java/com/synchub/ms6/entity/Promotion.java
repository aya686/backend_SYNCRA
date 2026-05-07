package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_id")
    private Long promoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePromotion type;

    @Column(nullable = false)
    private Double valeur;

    @Column(name = "datedebut")
    private LocalDateTime dateDebut;

    @Column(name = "datefin")
    private LocalDateTime dateFin;

    @Column(name = "codepromo", unique = true)
    private String codePromo;

    @ManyToMany(mappedBy = "promotions")
    @Builder.Default
    private List<Produit> produits = new ArrayList<>();

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return dateDebut != null && dateFin != null
                && !now.isBefore(dateDebut) && !now.isAfter(dateFin);
    }

    public Double calculerPrixPromo(Double prixOriginal) {
        if (!isActive()) return prixOriginal;
        if (type == TypePromotion.POURCENTAGE) {
            return prixOriginal * (1 - valeur / 100);
        } else {
            return Math.max(0, prixOriginal - valeur);
        }
    }

    public enum TypePromotion {
        POURCENTAGE, MONTANT_FIXE
    }
}
