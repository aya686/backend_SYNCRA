package com.synchub.ms6.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ProduitDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProduitRequest {
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
        private String nom;

        @Size(max = 3000, message = "La description ne doit pas dépasser 3000 caractères")
        private String description;

        @NotNull(message = "Le prix est obligatoire")
        @Positive(message = "Le prix doit être positif")
        private Double prix;

        private String categories;
        private String images;

        @NotNull(message = "L'ID de la boutique est obligatoire")
        private Long boutiqueId;

        private StockRequest stock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProduitResponse {
        private Long produitId;
        private String nom;
        private String description;
        private Double prix;
        private Double prixPromo;
        private String categories;
        private String images;
        private Boolean actif;
        private Boolean archive;  // Champ pour l'état archivé
        private Long boutiqueId;
        private StockResponse stock;
        private List<Long> promotionIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockRequest {
        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 0, message = "La quantité doit être positive ou nulle")
        private Integer quantite;

        @Min(value = 0, message = "Le seuil d'alerte doit être positif ou nul")
        private Integer seuilAlerte;

        private String entrepot;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockResponse {
        private Long stockId;
        private Integer quantite;
        private Integer seuilAlerte;
        private String entrepot;
        private Boolean alerteStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionRequest {
        @NotNull(message = "Le type de promotion est obligatoire")
        private String type;

        @NotNull(message = "La valeur est obligatoire")
        @Positive(message = "La valeur doit être positive")
        private Double valeur;

        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;

        @Size(max = 50, message = "Le code promo ne doit pas dépasser 50 caractères")
        private String codePromo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionResponse {
        private Long promoId;
        private String type;
        private Double valeur;
        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;
        private String codePromo;
        private Boolean active;
    }
}
