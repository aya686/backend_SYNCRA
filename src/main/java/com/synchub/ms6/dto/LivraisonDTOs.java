package com.synchub.ms6.dto;

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

public class LivraisonDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivraisonRequest {
        @NotNull(message = "L'ID de la commande est obligatoire")
        private Long commandeId;

        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        private String adresse;

        private String transporteur;
        private String tracking;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivraisonResponse {
        private Long livraisonId;
        private Long commandeId;
        private String adresse;
        private String transporteur;
        private String tracking;
        private String statut;
        private LocalDateTime dateExp;
        private LocalDateTime dateLiv;
        
        // Champs météo
        private Double weatherTemp;
        private String weatherCondition;
        private String weatherDescription;
        private String weatherIcon;
        private Boolean weatherAlert;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivraisonUpdateRequest {
        private String adresse;
        private String transporteur;
        private String tracking;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetourRequest {
        @NotBlank(message = "Le motif est obligatoire")
        @Size(max = 1000, message = "Le motif ne doit pas dépasser 1000 caractères")
        private String motif;

        private String condition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetourResponse {
        private Long retourId;
        private Long livraisonId;
        private String motif;
        private String statut;
        private LocalDateTime dateRetour;
        private String condition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemboursementRequest {
        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        private Double montant;

        @NotBlank(message = "La méthode est obligatoire")
        private String methode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemboursementResponse {
        private Long remboursementId;
        private Long retourId;
        private Double montant;
        private String methode;
        private String statut;
        private LocalDateTime dateTraitement;
    }
}
