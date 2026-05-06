package org.example.backend_pi.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoredResultDTO {

    // ─── IDENTIFIANTS ────────────────────────────────────────
    private Long id;
    private String resourceType;     // "MACHINE" ou "SERVICE"

    // ─── INFOS DE BASE ───────────────────────────────────────
    private String name;
    private String description;
    private String category;
    private String type;             // machineType ou serviceType
    private String transactionType;  // SALE / RENT / BOTH (machines)
    private Double price;
    private String priceUnit;
    private String location;
    private String contactInfo;
    private Double rating;
    private Integer reviewCount;
    private String availability;
    private List<String> imageUrls;
    private Integer stockQuantity;

    // ─── INFOS FOURNISSEUR ───────────────────────────────────
    private Long supplierId;
    private String supplierName;
    private String supplierCompanyName;
    private Boolean isInApp;

    // ─── SCORE GLOBAL ────────────────────────────────────────
    /**
     * Score global entre 0 et 100.
     * Plus il est élevé, plus le fournisseur correspond aux critères.
     */
    private Double totalScore;

    // ─── DÉTAIL DU SCORE PAR CRITÈRE ─────────────────────────
    private Double scoreType;         // 0–30 : match type/catégorie
    private Double scoreBudget;       // 0–25 : compatibilité budget
    private Double scoreLocation;     // 0–20 : proximité géographique
    private Double scoreRating;       // 0–15 : note des avis
    private Double scoreAvailability; // 0–10 : disponibilité et stock

    // ─── RAISONS LISIBLES (pour l'UI) ────────────────────────
    /**
     * Exemples :
     * - "✅ Type exact : Tour CNC"
     * - "💰 Prix dans votre budget (45 000 TND)"
     * - "📍 Localisation correspondante : Tunis"
     * - "⭐ Excellente réputation : 4.8/5 (32 avis)"
     * - "✅ Disponible immédiatement (stock : 3)"
     */
    private List<String> reasons;

    /** Niveau de recommandation : TOP, GOOD, AVERAGE, LOW */
    private String recommendationLevel;

    /** Badge affiché sur la carte : "Meilleur choix", "Bon rapport qualité/prix"... */
    private String badge;
    private String subCategory;        // ✅ NOUVEAU
    private String businessType;       // ✅ NOUVEAU

}
