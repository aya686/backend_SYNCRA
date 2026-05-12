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

@Entity
@Table(name = "commandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commande_id")
    private Long commandeId;

    @Column(name = "montanttotal", nullable = false)
    private Double montantTotal;

    @Convert(converter = com.synchub.ms6.converter.StatutCommandeConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    private LocalDateTime date;

    @Column(name = "adresselivraison")
    private String adresseLivraison;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Annulation annulation;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Livraison> livraisons = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promotion promotion;

    @Column(name = "codepromo")
    private String codePromo;

    @Column(name = "montantavantremise")
    private Double montantAvantRemise;

    @Column(name = "montantremise")
    private Double montantRemise;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }

    public void calculerMontantTotal() {
        this.montantTotal = lignes.stream()
                .mapToDouble(LigneCommande::getTotal)
                .sum();
    }

    public boolean canBeCancelled() {
        return statut == StatutCommande.EN_ATTENTE || statut == StatutCommande.CONFIRMEE;
    }

    public enum StatutCommande {
        EN_ATTENTE, CONFIRMEE, EN_COURS, LIVREE, ANNULEE
    }
}
