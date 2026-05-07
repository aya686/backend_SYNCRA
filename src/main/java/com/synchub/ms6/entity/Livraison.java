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
@Table(name = "livraisons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "livraison_id")
    private Long livraisonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @Column(nullable = false)
    private String adresse;

    private String transporteur;

    private String tracking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutLivraison statut = StatutLivraison.EN_PREPARATION;

    @Column(name = "dateexp")
    private LocalDateTime dateExp;

    @Column(name = "dateliv")
    private LocalDateTime dateLiv;

    @OneToMany(mappedBy = "livraison", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Retour> retours = new ArrayList<>();

    // Champs météo (stockés au moment de l'expédition)
    private Double weatherTemp;           // Température
    private String weatherCondition;      // Condition (Rain, Clear, etc.)
    private String weatherDescription;    // Description en français
    private String weatherIcon;           // Code icône OpenWeather
    private Boolean weatherAlert;         // true si conditions défavorables

    public boolean isDelivered() {
        return statut == StatutLivraison.LIVRE;
    }

    public enum StatutLivraison {
        EN_PREPARATION, EXPEDIE, EN_TRANSIT, LIVRE
    }
}
