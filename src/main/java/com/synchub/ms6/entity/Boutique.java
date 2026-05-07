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
@Table(name = "boutiques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Boutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boutique_id")
    private Long boutiqueId;

    @Column(nullable = false)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutBoutique statut = StatutBoutique.PENDING;

    private String theme;

    @Column(length = 100000)  // Taille grande pour accepter les images base64
    private String logo;

    @Column(name = "datecreation")
    private LocalDateTime dateCreation;

    @OneToOne(mappedBy = "boutique", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Configuration configuration;

    @OneToMany(mappedBy = "boutique", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StatsBoutique> stats = new ArrayList<>();

    @OneToMany(mappedBy = "boutique", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Produit> produits = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public enum StatutBoutique {
        ACTIVE, SUSPENDED, PENDING
    }
}
