package org.example.backend_pi.dto;
// dto/LoyaltyAccountDTO.java

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoyaltyAccountDTO {

    private Long userId;
    private String userName;

    // Points
    private Integer points;
    private Integer totalPointsEarned;
    private Integer totalPointsSpent;

    // Palier actuel
    private String tier;          // BRONZE | SILVER | GOLD | PLATINUM
    private String nextTier;
    private Integer pointsToNextTier;
    private Integer progressPercent;

    // Récompenses disponibles
    private Boolean freeDeliveryAvailable;
    private Boolean premiumAccessAvailable;
    private Integer discountPercent;

    // Historique des 10 dernières opérations
    private List<TxDTO> history;

    @Data
    public static class TxDTO {
        private Long id;
        private String type;
        private Integer pointsDelta;
        private Integer balanceAfter;
        private String description;
        private LocalDateTime createdAt;
    }
}