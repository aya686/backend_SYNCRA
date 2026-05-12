package org.example.backend_pi.dto;

// dto/PromoCodeDTO.java

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PromoCodeDTO {
    private Long id;                    // ✅ AJOUTER l'ID
    private String code;
    private String type;                // FREE_DELIVERY | DISCOUNT | PREMIUM
    private Integer discountPercent;
    private LocalDateTime expiresAt;    // ✅ Utiliser expiresAt au lieu de validUntil
    private Boolean used;
    private String message;             // Description lisible
    private Integer pointsCost;         // ✅ AJOUTER le coût en points
}