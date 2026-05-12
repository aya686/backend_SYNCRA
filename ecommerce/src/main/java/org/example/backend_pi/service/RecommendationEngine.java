package org.example.backend_pi.service;


import org.example.backend_pi.dto.RecommendationCriteriaDTO;
import org.example.backend_pi.dto.ScoredResultDTO;
import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.repository.MachineRepository;
import org.example.backend_pi.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // ─── POIDS PAR DÉFAUT (total = 100) ──────────────────────
    private static final int DEFAULT_WEIGHT_TYPE         = 25;
    private static final int DEFAULT_WEIGHT_BUDGET       = 22;
    private static final int DEFAULT_WEIGHT_LOCATION     = 18;
    private static final int DEFAULT_WEIGHT_RATING       = 15;
    private static final int DEFAULT_WEIGHT_AVAILABILITY = 10;
    private static final int DEFAULT_WEIGHT_BUSINESS     = 10; // ✅ NOUVEAU : type d'entreprise

    // ─── TUNISIE : villes et régions proches ─────────────────
    // Groupes géographiques : villes dans le même groupe = proximité maximale
    private static final Map<String, List<String>> LOCATION_GROUPS = new LinkedHashMap<>();
    static {
        LOCATION_GROUPS.put("GRAND_TUNIS",   Arrays.asList("tunis", "ariana", "ben arous", "manouba", "la marsa", "carthage", "sidi bou said"));
        LOCATION_GROUPS.put("CAP_BON",       Arrays.asList("nabeul", "hammamet", "kelibia", "menzel temime", "korba", "grombalia"));
        LOCATION_GROUPS.put("SAHEL",         Arrays.asList("sousse", "monastir", "mahdia", "msaken", "el alem", "ksibet"));
        LOCATION_GROUPS.put("SFAX",          Arrays.asList("sfax", "sakiet ezzit", "sakiet eddaier", "el ain", "chihia"));
        LOCATION_GROUPS.put("NORD_OUEST",    Arrays.asList("jendouba", "beja", "le kef", "siliana", "bousalem"));
        LOCATION_GROUPS.put("CENTRE",        Arrays.asList("kairouan", "kasserine", "sidi bouzid", "sbeitla"));
        LOCATION_GROUPS.put("SUD",           Arrays.asList("gabes", "medenine", "tataouine", "kebili", "tozeur", "gafsa"));
        LOCATION_GROUPS.put("BIZERTE",       Arrays.asList("bizerte", "mateur", "menzel bourguiba", "ras jebel"));
        LOCATION_GROUPS.put("ZAGHOUAN",      Arrays.asList("zaghouan", "enfida", "bir mcherga"));
    }

    // ═════════════════════════════════════════════════════════
    // POINT D'ENTRÉE PRINCIPAL
    // ═════════════════════════════════════════════════════════

    /**
     * Calcule les recommandations selon les critères fournis.
     * Retourne une liste triée par score décroissant.
     */
    public List<ScoredResultDTO> recommend(RecommendationCriteriaDTO criteria) {
        List<ScoredResultDTO> results = new ArrayList<>();

        boolean wantMachine = criteria.getResourceType() == null
                || "MACHINE".equalsIgnoreCase(criteria.getResourceType());
        boolean wantService = criteria.getResourceType() == null
                || "SERVICE".equalsIgnoreCase(criteria.getResourceType());

        if (wantMachine) {
            List<Machine> machines = machineRepository.findByValidationStatus("APPROVED");
            for (Machine m : machines) {
                ScoredResultDTO scored = scoreMachine(m, criteria);
                if (scored.getTotalScore() > 0) results.add(scored);
            }
        }

        if (wantService) {
            List<ServiceEntity> services = serviceRepository.findByValidationStatus("APPROVED");
            for (ServiceEntity s : services) {
                ScoredResultDTO scored = scoreService(s, criteria);
                if (scored.getTotalScore() > 0) results.add(scored);
            }
        }

        // Trier par score décroissant
        results.sort(Comparator.comparingDouble(ScoredResultDTO::getTotalScore).reversed());

        // Assigner les badges
        assignBadges(results);

        return results;
    }

    // ═════════════════════════════════════════════════════════
    // SCORING MACHINE
    // ═════════════════════════════════════════════════════════

    private ScoredResultDTO scoreMachine(Machine machine, RecommendationCriteriaDTO c) {
        ScoredResultDTO dto = new ScoredResultDTO();
        List<String> reasons = new ArrayList<>();

        // ── Poids effectifs ──────────────────────────────────
        int wType   = c.getWeightType()         != null ? c.getWeightType()         : DEFAULT_WEIGHT_TYPE;
        int wBudget = c.getWeightBudget()        != null ? c.getWeightBudget()       : DEFAULT_WEIGHT_BUDGET;
        int wLoc    = c.getWeightLocation()      != null ? c.getWeightLocation()     : DEFAULT_WEIGHT_LOCATION;
        int wRating = c.getWeightRating()        != null ? c.getWeightRating()       : DEFAULT_WEIGHT_RATING;
        int wAvail  = c.getWeightAvailability()  != null ? c.getWeightAvailability() : DEFAULT_WEIGHT_AVAILABILITY;
        int wBiz    = DEFAULT_WEIGHT_BUSINESS;

        // ── Critère 1 : Type / Catégorie ─────────────────────
        double typeScore = 0;
        boolean typeMatched = false;

        if (c.getMachineType() != null && machine.getType() != null) {
            if (c.getMachineType().equalsIgnoreCase(machine.getType().name())) {
                typeScore = wType;
                typeMatched = true;
                reasons.add("✅ Type exact : " + formatType(machine.getType().name()));
            }
        }
        if (c.getCategory() != null && machine.getCategory() != null) {
            if (c.getCategory().equalsIgnoreCase(machine.getCategory().name())) {
                typeScore = Math.max(typeScore, wType * 0.7);
                if (!typeMatched) {
                    reasons.add("🏷️ Catégorie correspondante : " + machine.getCategory().name());
                }
            }
        }
        if (c.getTransactionType() != null && machine.getTransactionType() != null) {
            String tx = machine.getTransactionType().name();
            boolean match = tx.equalsIgnoreCase(c.getTransactionType())
                    || "BOTH".equalsIgnoreCase(tx);
            if (match) {
                typeScore = Math.min(wType, typeScore + wType * 0.3);
                reasons.add("🔄 Mode : " + formatTransactionType(tx));
            }
        }
        // Si aucun filtre de type → score neutre de 50 %
        if (c.getMachineType() == null && c.getCategory() == null && c.getTransactionType() == null) {
            typeScore = wType * 0.5;
        }

        // ── Critère 2 : Budget ────────────────────────────────
        double budgetScore = scoreBudget(machine.getPrice(), c.getMinBudget(), c.getMaxBudget(),
                wBudget, machine.getName(), reasons);

        // ── Critère 3 : Localisation ──────────────────────────
        double locationScore = scoreLocation(machine.getLocation(), c.getPreferredLocation(),
                wLoc, reasons);

        // ── Critère 4 : Rating ────────────────────────────────
        double ratingScore = scoreRating(machine.getRating(), machine.getReviewCount(),
                c.getMinRating(), wRating, reasons);

        // ── Critère 5 : Disponibilité ─────────────────────────
        double availScore = scoreMachineAvailability(machine, c, wAvail, reasons);

        // ── Critère 6 : Type d'entreprise ────────────────────
        double bizScore = scoreBusinessType(
                machine.getBusinessType() != null ? machine.getBusinessType().name() : null,
                c.getBusinessType(), wBiz, reasons);

        // ── Bonus sous-catégorie ──────────────────────────────
        double subCatBonus = 0;
        if (c.getSubCategory() != null && !c.getSubCategory().isBlank()
                && machine.getSubCategory() != null) {
            if (c.getSubCategory().equalsIgnoreCase(machine.getSubCategory())) {
                subCatBonus = 5;
                reasons.add("🎯 Sous-catégorie exacte : " + machine.getSubCategory());
            }
        }

        // ── Total ─────────────────────────────────────────────
        double total = typeScore + budgetScore + locationScore + ratingScore + availScore + bizScore + subCatBonus;
        total = Math.min(100, Math.round(total * 10.0) / 10.0);

        // ── Remplir le DTO ────────────────────────────────────
        dto.setId(machine.getId());
        dto.setResourceType("MACHINE");
        dto.setName(machine.getName());
        dto.setDescription(machine.getDescription());
        dto.setCategory(machine.getCategory() != null ? machine.getCategory().name() : null);
        dto.setType(machine.getType() != null ? machine.getType().name() : null);
        dto.setTransactionType(machine.getTransactionType() != null ? machine.getTransactionType().name() : null);
        dto.setPrice(machine.getPrice());
        dto.setPriceUnit(machine.getPriceUnit());
        dto.setLocation(machine.getLocation());
        dto.setContactInfo(machine.getContactInfo());
        dto.setRating(machine.getRating() != null ? machine.getRating() : 0.0);
        dto.setReviewCount(machine.getReviewCount() != null ? machine.getReviewCount() : 0);
        dto.setAvailability(machine.getAvailability() != null ? machine.getAvailability().name() : "UNKNOWN");
        dto.setImageUrls(machine.getImageUrls());
        dto.setStockQuantity(machine.getStockQuantity());
        dto.setSupplierId(machine.getSupplierId());
        dto.setSupplierName(machine.getSupplierName());
        dto.setSupplierCompanyName(machine.getSupplierCompanyName());
        dto.setIsInApp(machine.getIsInApp());
        dto.setSubCategory(machine.getSubCategory());
        dto.setBusinessType(machine.getBusinessType() != null ? machine.getBusinessType().name() : null);
        dto.setTotalScore(total);
        dto.setScoreType(typeScore);
        dto.setScoreBudget(budgetScore);
        dto.setScoreLocation(locationScore);
        dto.setScoreRating(ratingScore);
        dto.setScoreAvailability(availScore);
        dto.setRecommendationLevel(getLevel(total));
        dto.setReasons(reasons);

        return dto;
    }

    // ═════════════════════════════════════════════════════════
    // SCORING SERVICE
    // ═════════════════════════════════════════════════════════

    private ScoredResultDTO scoreService(ServiceEntity service, RecommendationCriteriaDTO c) {
        ScoredResultDTO dto = new ScoredResultDTO();
        List<String> reasons = new ArrayList<>();

        int wType   = c.getWeightType()         != null ? c.getWeightType()         : DEFAULT_WEIGHT_TYPE;
        int wBudget = c.getWeightBudget()        != null ? c.getWeightBudget()       : DEFAULT_WEIGHT_BUDGET;
        int wLoc    = c.getWeightLocation()      != null ? c.getWeightLocation()     : DEFAULT_WEIGHT_LOCATION;
        int wRating = c.getWeightRating()        != null ? c.getWeightRating()       : DEFAULT_WEIGHT_RATING;
        int wAvail  = c.getWeightAvailability()  != null ? c.getWeightAvailability() : DEFAULT_WEIGHT_AVAILABILITY;
        int wBiz    = DEFAULT_WEIGHT_BUSINESS;
        double typeScore = 0;

        if (c.getServiceType() != null && service.getServiceType() != null) {
            if (c.getServiceType().equalsIgnoreCase(service.getServiceType().name())) {
                typeScore = wType;
                reasons.add("✅ Type exact : " + formatType(service.getServiceType().name()));
            } else {
                typeScore = wType * 0.2;
            }
        }
        if (c.getCategory() != null && service.getCategory() != null) {
            if (c.getCategory().equalsIgnoreCase(service.getCategory().name())) {
                typeScore = Math.max(typeScore, wType * 0.6);
                reasons.add("🏷️ Catégorie : " + service.getCategory().name());
            }
        }
        if (c.getServiceType() == null && c.getCategory() == null) {
            typeScore = wType * 0.5;
        }

        // ── Budget ────────────────────────────────────────────
        double budgetScore = scoreBudget(service.getBasePrice(), c.getMinBudget(), c.getMaxBudget(),
                wBudget, service.getName(), reasons);

        // ── Localisation ──────────────────────────────────────
        double locationScore = scoreLocation(service.getLocation(), c.getPreferredLocation(),
                wLoc, reasons);

        // ── Rating ────────────────────────────────────────────
        double ratingScore = scoreRating(service.getRating(), service.getReviewCount(),
                c.getMinRating(), wRating, reasons);

        // ── Disponibilité service ─────────────────────────────
        double availScore = 0;
        if (service.getAvailability() != null) {
            String avail = service.getAvailability().name();
            if ("AVAILABLE".equals(avail)) {
                availScore = wAvail;
                reasons.add("✅ Service disponible immédiatement");
            } else if ("RESERVED".equals(avail)) {
                availScore = wAvail * 0.4;
                reasons.add("⏳ Service partiellement disponible (réservé)");
            } else {
                if (Boolean.FALSE.equals(c.getRequiresAvailability())) {
                    availScore = wAvail * 0.2;
                }
                reasons.add("❌ Service temporairement indisponible");
            }
        }

        double total = Math.min(100, Math.round(
                (typeScore + budgetScore + locationScore + ratingScore + availScore) * 10.0) / 10.0);

        // ── Type d'entreprise ──────────────────────────────────
        double bizScore = scoreBusinessType(
                service.getBusinessType() != null ? service.getBusinessType().name() : null,
                c.getBusinessType(), wBiz, reasons);

        // ── Bonus sous-catégorie ──────────────────────────────
        double subCatBonus = 0;
        if (c.getSubCategory() != null && !c.getSubCategory().isBlank()
                && service.getSubCategory() != null) {
            if (c.getSubCategory().equalsIgnoreCase(service.getSubCategory())) {
                subCatBonus = 5;
                reasons.add("🎯 Sous-catégorie exacte : " + service.getSubCategory());
            }
        }

        total = Math.min(100, Math.round((total + bizScore + subCatBonus) * 10.0) / 10.0);

        dto.setId(service.getId());
        dto.setResourceType("SERVICE");
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setCategory(service.getCategory() != null ? service.getCategory().name() : null);
        dto.setType(service.getServiceType() != null ? service.getServiceType().name() : null);
        dto.setPrice(service.getBasePrice());
        dto.setPriceUnit(service.getPriceUnit());
        dto.setLocation(service.getLocation());
        dto.setContactInfo(service.getContactInfo());
        dto.setRating(service.getRating() != null ? service.getRating() : 0.0);
        dto.setReviewCount(service.getReviewCount() != null ? service.getReviewCount() : 0);
        dto.setAvailability(service.getAvailability() != null ? service.getAvailability().name() : "UNKNOWN");
        dto.setImageUrls(service.getImageUrls());
        dto.setSupplierId(service.getProviderId());
        dto.setSupplierName(service.getProviderName());
        dto.setSupplierCompanyName(service.getProviderCompanyName());
        dto.setIsInApp(service.getIsInApp());
        dto.setSubCategory(service.getSubCategory());
        dto.setBusinessType(service.getBusinessType() != null ? service.getBusinessType().name() : null);
        dto.setTotalScore(total);
        dto.setScoreType(typeScore);
        dto.setScoreBudget(budgetScore);
        dto.setScoreLocation(locationScore);
        dto.setScoreRating(ratingScore);
        dto.setScoreAvailability(availScore);
        dto.setRecommendationLevel(getLevel(total));
        dto.setReasons(reasons);

        return dto;
    }

    // ═════════════════════════════════════════════════════════
    // MÉTHODES DE SCORING PARTAGÉES
    // ═════════════════════════════════════════════════════════

    /** Scoring budget : plein si dans le budget, dégressif sinon */
    private double scoreBudget(Double price, Double minBudget, Double maxBudget,
                               int weight, String name, List<String> reasons) {
        if (price == null) return weight * 0.3;

        boolean hasMax = maxBudget != null && maxBudget > 0;
        boolean hasMin = minBudget != null && minBudget > 0;

        if (!hasMax && !hasMin) {
            // Pas de contrainte budget → score neutre
            reasons.add("💰 Prix : " + formatPrice(price) + " TND");
            return weight * 0.5;
        }

        if (hasMax && price > maxBudget) {
            // Dépasse le budget
            double overRatio = price / maxBudget;
            if (overRatio > 2.0) return 0; // plus du double → score nul
            double penalty = (overRatio - 1.0); // 0 à 1
            double score = Math.max(0, weight * (1 - penalty));
            reasons.add("⚠️ Prix légèrement au-dessus du budget (" + formatPrice(price) + " TND vs max " + formatPrice(maxBudget) + " TND)");
            return Math.round(score * 10.0) / 10.0;
        }

        if (hasMin && price < minBudget) {
            reasons.add("💡 Prix très compétitif : " + formatPrice(price) + " TND");
            return weight * 0.8; // sous le min → légèrement moins bien (qualité ?)
        }

        // Dans le budget → score complet
        reasons.add("💰 Prix dans votre budget : " + formatPrice(price) + " TND");
        return weight;
    }

    /** Scoring localisation : même ville = max, même région = 60%, reste = 20% */
    private double scoreLocation(String itemLocation, String preferredLocation,
                                 int weight, List<String> reasons) {
        if (preferredLocation == null || preferredLocation.isBlank()) {
            return weight * 0.5;
        }
        if (itemLocation == null || itemLocation.isBlank()) {
            return weight * 0.2;
        }

        String itemLow  = itemLocation.toLowerCase().trim();
        String prefLow  = preferredLocation.toLowerCase().trim();

        // Correspondance exacte (ou contient)
        if (itemLow.contains(prefLow) || prefLow.contains(itemLow)) {
            reasons.add("📍 Localisation correspondante : " + itemLocation);
            return weight;
        }

        // Même groupe géographique
        String itemGroup = getLocationGroup(itemLow);
        String prefGroup = getLocationGroup(prefLow);

        if (itemGroup != null && itemGroup.equals(prefGroup)) {
            reasons.add("🗺️ Région proche : " + itemLocation + " (même zone que " + preferredLocation + ")");
            return weight * 0.6;
        }

        // Zones adjacentes (heuristique simple)
        if (areAdjacentRegions(itemGroup, prefGroup)) {
            reasons.add("🗺️ Zone accessible : " + itemLocation);
            return weight * 0.3;
        }

        reasons.add("📍 Localisation : " + itemLocation);
        return weight * 0.15;
    }

    /** Scoring rating : progressif entre 0 et le poids max */
    private double scoreRating(Double rating, Integer reviewCount,
                               Double minRating, int weight, List<String> reasons) {
        if (rating == null || rating == 0) {
            reasons.add("ℹ️ Pas encore d'avis");
            return weight * 0.3; // score neutre pour les nouveaux
        }

        // Si le minimum requis n'est pas atteint → score nul
        if (minRating != null && rating < minRating) {
            reasons.add("⚠️ Note en dessous du minimum requis (" + rating + "/5)");
            return 0;
        }

        // Score progressif : 0★ = 0, 5★ = poids max
        double ratingRatio = rating / 5.0;
        double baseScore = weight * ratingRatio;

        // Bonus si beaucoup d'avis (fiabilité)
        double reliabilityBonus = 0;
        if (reviewCount != null) {
            if (reviewCount >= 50)       reliabilityBonus = weight * 0.15;
            else if (reviewCount >= 20)  reliabilityBonus = weight * 0.10;
            else if (reviewCount >= 10)  reliabilityBonus = weight * 0.05;
        }

        double total = Math.min(weight, baseScore + reliabilityBonus);

        if (rating >= 4.5) {
            reasons.add("⭐ Excellente réputation : " + rating + "/5 (" + (reviewCount != null ? reviewCount : 0) + " avis)");
        } else if (rating >= 3.5) {
            reasons.add("⭐ Bonne réputation : " + rating + "/5 (" + (reviewCount != null ? reviewCount : 0) + " avis)");
        } else {
            reasons.add("⭐ Note : " + rating + "/5 (" + (reviewCount != null ? reviewCount : 0) + " avis)");
        }

        return Math.round(total * 10.0) / 10.0;
    }

    /** Scoring disponibilité machine (statut + stock) */
    private double scoreMachineAvailability(Machine machine, RecommendationCriteriaDTO c,
                                            int weight, List<String> reasons) {
        if (machine.getAvailability() == null) return weight * 0.2;

        String avail = machine.getAvailability().name();
        double score;

        switch (avail) {
            case "AVAILABLE":
                score = weight;
                // Vérifier le stock si une quantité est requise
                if (c.getRequiredQuantity() != null && machine.getStockQuantity() != null) {
                    if (machine.getStockQuantity() >= c.getRequiredQuantity()) {
                        reasons.add("✅ Stock suffisant : " + machine.getStockQuantity()
                                + " unités (besoin : " + c.getRequiredQuantity() + ")");
                    } else {
                        score = weight * 0.4;
                        reasons.add("⚠️ Stock insuffisant : " + machine.getStockQuantity()
                                + " unités (besoin : " + c.getRequiredQuantity() + ")");
                    }
                } else {
                    reasons.add("✅ Disponible immédiatement"
                            + (machine.getStockQuantity() != null ? " (stock : " + machine.getStockQuantity() + ")" : ""));
                }
                break;

            case "RESERVED":
                score = weight * 0.4;
                reasons.add("⏳ Partiellement réservé — disponibilité sous réserve");
                break;

            case "UNAVAILABLE":
            default:
                score = Boolean.FALSE.equals(c.getRequiresAvailability()) ? weight * 0.1 : 0;
                reasons.add("❌ Non disponible actuellement");
                break;
        }

        return score;
    }

    // ═════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═════════════════════════════════════════════════════════

    /** Scoring type d'entreprise */
    private double scoreBusinessType(String itemBizType, String preferredBizType,
                                     int weight, List<String> reasons) {
        if (preferredBizType == null || preferredBizType.isBlank()) {
            return weight * 0.5; // Pas de préférence → score neutre
        }
        if (itemBizType == null || itemBizType.isBlank()) {
            return weight * 0.2; // Pas renseigné → score minimal
        }
        if (preferredBizType.equalsIgnoreCase(itemBizType)) {
            reasons.add("🏭 Type d'entreprise correspondant : " + formatBusinessType(itemBizType));
            return weight;
        }
        // Compatibilités partielles
        boolean partial = false;
        if ("MANUFACTURER".equals(preferredBizType) && "CUSTOM_MANUFACTURER".equals(itemBizType)) partial = true;
        if ("CUSTOM_MANUFACTURER".equals(preferredBizType) && "MANUFACTURER".equals(itemBizType)) partial = true;
        if ("DISTRIBUTOR".equals(preferredBizType) && "WHOLESALER".equals(itemBizType)) partial = true;
        if ("WHOLESALER".equals(preferredBizType) && "DISTRIBUTOR".equals(itemBizType)) partial = true;
        if (partial) {
            reasons.add("🏭 Type d'entreprise similaire : " + formatBusinessType(itemBizType));
            return weight * 0.6;
        }
        return weight * 0.1;
    }

    private String formatBusinessType(String type) {
        switch (type) {
            case "MANUFACTURER":        return "Fabricant/Producteur";
            case "CUSTOM_MANUFACTURER": return "Fabricant spécifique client";
            case "DISTRIBUTOR":         return "Distributeur";
            case "SERVICE_PROVIDER":    return "Prestataire de services";
            case "WHOLESALER":          return "Grossiste";
            default: return type;
        }
    }

    /** Détermine le groupe géographique d'une ville */
    private String getLocationGroup(String city) {
        if (city == null) return null;
        String low = city.toLowerCase().trim();
        for (Map.Entry<String, List<String>> entry : LOCATION_GROUPS.entrySet()) {
            for (String loc : entry.getValue()) {
                if (low.contains(loc) || loc.contains(low)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /** Régions adjacentes en Tunisie */
    private boolean areAdjacentRegions(String g1, String g2) {
        if (g1 == null || g2 == null) return false;
        Map<String, List<String>> adjacent = new HashMap<>();
        adjacent.put("GRAND_TUNIS", Arrays.asList("CAP_BON", "BIZERTE", "ZAGHOUAN", "NORD_OUEST"));
        adjacent.put("CAP_BON",     Arrays.asList("GRAND_TUNIS", "SAHEL", "ZAGHOUAN"));
        adjacent.put("SAHEL",       Arrays.asList("CAP_BON", "SFAX", "CENTRE"));
        adjacent.put("SFAX",        Arrays.asList("SAHEL", "CENTRE", "SUD"));
        adjacent.put("NORD_OUEST",  Arrays.asList("GRAND_TUNIS", "BIZERTE", "CENTRE"));
        adjacent.put("CENTRE",      Arrays.asList("SAHEL", "SFAX", "SUD", "NORD_OUEST"));
        adjacent.put("SUD",         Arrays.asList("SFAX", "CENTRE"));
        adjacent.put("BIZERTE",     Arrays.asList("GRAND_TUNIS", "NORD_OUEST"));
        adjacent.put("ZAGHOUAN",    Arrays.asList("GRAND_TUNIS", "CAP_BON", "CENTRE"));
        List<String> adj = adjacent.get(g1);
        return adj != null && adj.contains(g2);
    }

    /** Détermine le niveau de recommandation */
    private String getLevel(double score) {
        if (score >= 80) return "TOP";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "AVERAGE";
        return "LOW";
    }

    /** Assigne les badges selon le classement et le score */
    private void assignBadges(List<ScoredResultDTO> results) {
        if (results.isEmpty()) return;

        for (int i = 0; i < results.size(); i++) {
            ScoredResultDTO r = results.get(i);
            double score = r.getTotalScore();

            if (i == 0 && score >= 75) {
                r.setBadge("🏆 Meilleur choix");
            } else if (i == 1 && score >= 65) {
                r.setBadge("🥈 Excellent");
            } else if (score >= 80) {
                r.setBadge("⭐ Top recommandé");
            } else if (score >= 65) {
                r.setBadge("👍 Très bon choix");
            } else if (score >= 50) {
                r.setBadge("✅ Bon rapport qualité/prix");
            } else if (score >= 35) {
                r.setBadge("💡 Alternative");
            } else {
                r.setBadge(null);
            }
        }
    }

    private String formatPrice(Double price) {
        if (price == null) return "—";
        return String.format("%,.0f", price);
    }

    private String formatType(String type) {
        if (type == null) return "—";
        return type.replace("_", " ").toLowerCase();
    }

    private String formatTransactionType(String tx) {
        switch (tx) {
            case "SALE": return "À vendre";
            case "RENT": return "À louer";
            case "BOTH": return "Vente & location";
            default: return tx;
        }
    }
}