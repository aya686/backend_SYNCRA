package com.synchub.ms6.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité pour tracker le comportement des utilisateurs
 * Utilisée pour le système de recommandation collaboratif
 */
@Entity
@Table(name = "user_behaviors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "behavior_id")
    private Long behaviorId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "produit_id")
    private Long produitId;

    @Column(name = "session_id")
    private String sessionId;

    @Column
    private String type; // VIEW, ADD_TO_CART, PURCHASE, WISHLIST

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column
    private String referrer;

    @Column(name = "user_agent")
    private String userAgent;

    @Column
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
