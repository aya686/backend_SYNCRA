package org.example.backend_pi.dto;

import lombok.Data;

@Data
public class RecommendationCriteriaDTO {

    // ─── Type de ressource ────────────────────────────────────
    private String resourceType;       // MACHINE | SERVICE | null (les deux)

    // ─── Filtres Machine ──────────────────────────────────────
    private String machineType;        // EQUIPMENT | RAW_MATERIAL | PART_ACCESSORY
    private String transactionType;    // SALE | RENT | BOTH

    // ─── Filtres Service ──────────────────────────────────────
    private String serviceType;        // FABRICATION | REPARATION | CONSULTATION | ...

    // ─── Filtres communs ──────────────────────────────────────
    private String category;           // INDUSTRIELLE | AGRICOLE | ...
    private String subCategory;        // ✅ NOUVEAU : sous-catégorie (tractors, irrigation...)
    private String businessType;       // ✅ NOUVEAU : MANUFACTURER | DISTRIBUTOR | ...

    // ─── Budget ───────────────────────────────────────────────
    private Double maxBudget;
    private Double minBudget;

    // ─── Localisation ─────────────────────────────────────────
    private String preferredLocation;

    // ─── Qualité ──────────────────────────────────────────────
    private Double minRating;

    // ─── Disponibilité ────────────────────────────────────────
    private Boolean requiresAvailability;
    private Integer requiredQuantity;

    // ─── Poids personnalisés (optionnel, défaut dans le moteur) ─
    private Integer weightType;
    private Integer weightBudget;
    private Integer weightLocation;
    private Integer weightRating;
    private Integer weightAvailability;
}