package org.example.backend_pi.entity;

// entity/PromoCode.java
// Représente un code promo généré automatiquement depuis les points de fidélité

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code alphanumérique unique ex: LIVRAISON-A7B3, REDUC10-X9K2 */
    @Column(name = "code", unique = true, nullable = false, length = 30)
    private String code;

    /** ID de l'utilisateur propriétaire du code */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Type de récompense :
     *   FREE_DELIVERY → livraison gratuite (0 TND)
     *   DISCOUNT      → réduction en % sur le total
     *   PREMIUM       → accès premium (pas de réduction monétaire)
     */
    @Column(name = "type", length = 20, nullable = false)
    private String type;

    /** % de réduction (uniquement pour type=DISCOUNT, sinon 0) */
    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    /** Points fidélité déduits lors de la génération du code */
    @Column(name = "points_cost")
    private Integer pointsCost = 0;

    /** Le code a-t-il été utilisé ? */
    @Column(name = "used")
    private Boolean used = false;

    /** Date d'utilisation */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** Date d'expiration (7 jours après génération) */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Expiration à 7 jours par défaut
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7);
        }
        if (used == null) {
            used = false;
        }
    }

    /** Vérifie si le code est encore valide */
    public boolean isValid() {
        return !Boolean.TRUE.equals(used)
                && expiresAt != null
                && LocalDateTime.now().isBefore(expiresAt);
    }

    /** ✅ AJOUTER la méthode isUsed() pour l'interface */
    public boolean isUsed() {
        return Boolean.TRUE.equals(used);
    }
}