package com.synchub.ms6.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CommandeDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommandeRequest {
        @NotBlank(message = "L'adresse de livraison est obligatoire")
        @Size(max = 500, message = "L'adresse ne doit pas dépasser 500 caractères")
        private String adresseLivraison;

        @NotEmpty(message = "Au moins une ligne de commande est requise")
        @Valid
        private List<LigneCommandeRequest> lignes;

        private String codePromo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneCommandeRequest {
        @NotNull(message = "L'ID du produit est obligatoire")
        private Long produitId;

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        private Integer quantite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommandeResponse {
        private Long commandeId;
        private Double montantTotal;
        private String statut;
        private LocalDateTime date;
        private String adresseLivraison;
        private List<LigneCommandeResponse> lignes;
        private String codePromo;
        private Double montantAvantRemise;
        private Double montantRemise;
        private String promotionType;
        private Double promotionValeur;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneCommandeResponse {
        private Long ligneId;
        private Long produitId;
        private String nomProduit;
        private Integer quantite;
        private Double prixUnitaire;
        private Double total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatutUpdateRequest {
        @NotBlank(message = "Le statut est obligatoire")
        private String statut;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnulationRequest {
        @NotBlank(message = "Le motif est obligatoire")
        @Size(max = 1000, message = "Le motif ne doit pas dépasser 1000 caractères")
        private String motif;

        private Boolean rembourse;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnulationResponse {
        private Long annulationId;
        private Long commandeId;
        private String motif;
        private LocalDateTime dateAnnulation;
        private Boolean rembourse;
        private String statut;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromoValidationResponse {
        private boolean valid;
        private String message;
        private String codePromo;
        private String type;
        private Double valeur;
        private Double montantRemise;
        private Double montantAvantRemise;
        private Double montantApresRemise;
    }
}
