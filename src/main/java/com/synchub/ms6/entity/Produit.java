package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produit_id")
    private Long produitId;

    @Column(nullable = false)
    private String nom;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false)
    private Double prix;

    private String categories;

    @Column(length = 50000)  // Taille grande pour accepter plusieurs images base64
    private String images;

    @Builder.Default
    private Boolean actif = true;

    @Builder.Default
    private Boolean archive = false;  // Champ pour archiver le produit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    @OneToOne(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stock stock;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "produit_promotion",
            joinColumns = @JoinColumn(name = "produit_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_id")
    )
    @Builder.Default
    private List<Promotion> promotions = new ArrayList<>();

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneCommande> lignesCommande = new ArrayList<>();
}
