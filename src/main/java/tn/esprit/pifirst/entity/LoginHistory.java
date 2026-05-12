package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Informations de connexion
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    private LocalDateTime loginTime;
    private Boolean isSuccessful;

    // Géolocalisation
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;

    // Appareil
    private String deviceType;
    private String os;
    private String browser;

    // Sécurité
    private Integer riskScore;
    private String failureReason;

    // 2FA
    private Boolean twoFactorRequired;
    private String twoFactorCode;
    private Boolean twoFactorValidated;

    // Géolocalisation supplémentaire
    private String countryCode;
    private String region;
    private String zip;
    private String isp;

    // ✅ NOUVEAUX CHAMPS POUR LA TRACABILITÉ (PROBLÈME 4)
    @Column(columnDefinition = "INT DEFAULT NULL")
    private Integer isolationScore;

    @Column(columnDefinition = "INT DEFAULT NULL")
    private Integer randomForestScore;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL")
    private String mlDecision;      // "NORMAL", "2FA_REQUIRED", "BLOCKED"

    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL")
    private String ruleTriggered;   // "none", "first_login", "impossible_travel", "fallback"

    // Ajoute ce champ dans l'entité LoginHistory.java
    @Column(columnDefinition = "TEXT")
    private String shapReason;

    // Getters et Setters
    public String getShapReason() { return shapReason; }
    public void setShapReason(String shapReason) { this.shapReason = shapReason; }
}