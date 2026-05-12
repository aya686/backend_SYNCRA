package org.example.backend_pi.entity;
// entity/LoyaltyTransaction.java
// Historique de chaque opération de points (gain / utilisation)

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions")
@Data
@NoArgsConstructor
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Type d'opération :
     *   EARN_ORDER        – points gagnés lors d'un achat
     *   EARN_FIRST_ORDER  – bonus premier achat
     *   REDEEM_DISCOUNT   – points utilisés pour une réduction
     *   REDEEM_DELIVERY   – points utilisés pour livraison gratuite
     *   REDEEM_PREMIUM    – points utilisés pour accès premium
     *   EXPIRE            – points expirés
     *   ADMIN_ADJUST      – ajustement manuel admin
     */
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    /** Nombre de points (positif = gain, négatif = dépense) */
    @Column(name = "points_delta", nullable = false)
    private Integer pointsDelta;

    /** Points restants après cette transaction */
    @Column(name = "balance_after")
    private Integer balanceAfter;

    /** Description lisible */
    @Column(name = "description", length = 300)
    private String description;

    /** ID de la commande associée (si applicable) */
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}