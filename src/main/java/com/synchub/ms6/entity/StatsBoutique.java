package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stats_boutiques")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsBoutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long statsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boutique_id", nullable = false)
    private Boutique boutique;

    @Column(name = "totalventes")
    @Builder.Default
    private Double totalVentes = 0.0;

    @Column(name = "totalcommandes")
    @Builder.Default
    private Integer totalCommandes = 0;

    @Column(name = "notemoyenne")
    @Builder.Default
    private Double noteMoyenne = 0.0;

    @Column(name = "datecalcul")
    private LocalDateTime dateCalcul;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        dateCalcul = LocalDateTime.now();
    }
}
