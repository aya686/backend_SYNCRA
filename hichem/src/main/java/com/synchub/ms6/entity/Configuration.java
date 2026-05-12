package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boutique_id", nullable = false, unique = true)
    private Boutique boutique;

    private String livraison;

    @Column(name = "paiementaccepte")
    private String paiementAccepte;

    @Column(length = 2000)
    private String politique;

    private String langue;
}
