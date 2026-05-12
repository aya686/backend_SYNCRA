package org.example.backend_pi.entity;
// entity/LoyaltyAccount.java
// Compte de fidélité d'un utilisateur — lié à ses achats

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_accounts")
@Data
@NoArgsConstructor
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de l'utilisateur propriétaire du compte */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    /** Nom de l'utilisateur (dénormalisé pour les affichages) */
    @Column(name = "user_name")
    private String userName;

    /** Points disponibles actuellement */
    @Column(name = "points", nullable = false)
    private Integer points = 0;

    /** Total des points cumulés depuis la création du compte */
    @Column(name = "total_points_earned")
    private Integer totalPointsEarned = 0;

    /** Total des points utilisés (réductions, livraisons...) */
    @Column(name = "total_points_spent")
    private Integer totalPointsSpent = 0;

    /**
     * Niveau de fidélité calculé automatiquement :
     * BRONZE    : 0–499 pts
     * SILVER    : 500–1499 pts
     * GOLD      : 1500–3999 pts
     * PLATINUM  : 4000+ pts
     */
    @Column(name = "tier")
    private String tier = "BRONZE";

    /** A droit à la livraison gratuite (≥ 300 pts) */
    @Column(name = "free_delivery_available")
    private Boolean freeDeliveryAvailable = false;

    /** A accès aux fournisseurs premium (≥ 800 pts) */
    @Column(name = "premium_access_available")
    private Boolean premiumAccessAvailable = false;

    /** Réduction disponible en % (calculée depuis les points) */
    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalculateTier();
        recalculateRewards();
    }

    /** Recalcule le palier selon les points totaux gagnés */
    public void recalculateTier() {
        if (totalPointsEarned >= 4000)      this.tier = "PLATINUM";
        else if (totalPointsEarned >= 1500) this.tier = "GOLD";
        else if (totalPointsEarned >= 500)  this.tier = "SILVER";
        else                                this.tier = "BRONZE";
    }

    /** Recalcule les récompenses disponibles selon les points courants */
    public void recalculateRewards() {
        // Livraison gratuite : 300 pts
        this.freeDeliveryAvailable = (this.points >= 300);
        // Accès premium : 800 pts
        this.premiumAccessAvailable = (this.points >= 800);
        // Réduction : 100 pts = 1% (max 15%)
        this.discountPercent = Math.min(15, this.points / 100);
    }
}