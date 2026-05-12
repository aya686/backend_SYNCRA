package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "remboursements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Remboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "remboursement_id")
    private Long remboursementId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retour_id", nullable = false, unique = true)
    private Retour retour;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodeRemboursement methode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutRemboursement statut = StatutRemboursement.EN_ATTENTE;

    @Column(name = "datetraitement")
    private LocalDateTime dateTraitement;

    @PrePersist
    protected void onCreate() {
        if (statut == StatutRemboursement.TRAITE) {
            dateTraitement = LocalDateTime.now();
        }
    }

    public enum MethodeRemboursement {
        CARTE, VIREMENT, AVOIR
    }

    public enum StatutRemboursement {
        EN_ATTENTE, TRAITE, REJETE
    }
}
