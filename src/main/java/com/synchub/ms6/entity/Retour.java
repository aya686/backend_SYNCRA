package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "retours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Retour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "retour_id")
    private Long retourId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livraison_id", nullable = false)
    private Livraison livraison;

    @Column(nullable = false)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRetour statut = StatutRetour.DEMANDE;

    @Column(name = "dateretour")
    private LocalDateTime dateRetour;

    @Column(name = "`condition`")
    private String condition;

    @OneToOne(mappedBy = "retour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Remboursement remboursement;

    @PrePersist
    protected void onCreate() {
        dateRetour = LocalDateTime.now();
    }

    public boolean isApproved() {
        return statut == StatutRetour.APPROUVE || statut == StatutRetour.TRAITE;
    }

    public enum StatutRetour {
        DEMANDE, APPROUVE, RECU, TRAITE, REJETE
    }
}
