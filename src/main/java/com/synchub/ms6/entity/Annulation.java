package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "annulations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Annulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annulation_id")
    private Long annulationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @Column(nullable = false)
    private String motif;

    @Column(name = "dateannulation")
    private LocalDateTime dateAnnulation;

    private Boolean rembourse;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutAnnulation statut = StatutAnnulation.DEMANDEE;

    @PrePersist
    protected void onCreate() {
        dateAnnulation = LocalDateTime.now();
    }

    public boolean requiresRemboursement() {
        return Boolean.TRUE.equals(rembourse);
    }

    public enum StatutAnnulation {
        DEMANDEE, APPROUVEE, REJETEE, TRAITEE
    }
}
