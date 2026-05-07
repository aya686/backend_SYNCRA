package com.synchub.ms6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BoutiqueDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoutiqueRequest {
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        private String nom;

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        private String description;

        private String theme;
        private String logo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoutiqueResponse {
        private Long boutiqueId;
        private String nom;
        private String description;
        private String statut;
        private String theme;
        private String logo;
        private LocalDateTime dateCreation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigurationRequest {
        private String livraison;
        private String paiementAccepte;
        @Size(max = 2000, message = "La politique ne doit pas dépasser 2000 caractères")
        private String politique;
        private String langue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigurationResponse {
        private Long configId;
        private String livraison;
        private String paiementAccepte;
        private String politique;
        private String langue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsBoutiqueResponse {
        private Long statsId;
        private Double totalVentes;
        private Integer totalCommandes;
        private Double noteMoyenne;
        private LocalDateTime dateCalcul;
    }
}
