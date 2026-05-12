package org.example.backend_pi.service;

// service/LoyaltyService.java
// ══════════════════════════════════════════════════════════════
// MOTEUR DE FIDÉLITÉ + CLASSEMENT TOP MACHINES/SERVICES
// ══════════════════════════════════════════════════════════════

import org.example.backend_pi.dto.LoyaltyAccountDTO;
import org.example.backend_pi.dto.PromoCodeDTO;
import org.example.backend_pi.dto.TopItemDTO;
import org.example.backend_pi.entity.*;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.enums.RequestStatus;
import org.example.backend_pi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoyaltyService {

    // ─── RÈGLE DE POINTS ─────────────────────────────────────
    // 1 TND dépensé = 1 point
    // Bonus palier Silver : +20%,  Gold : +50%,  Platinum : +100%
    private static final double BASE_POINTS_PER_TND = 1.0;

    // Seuils de récompense
    private static final int FREE_DELIVERY_THRESHOLD  = 300;
    private static final int PREMIUM_ACCESS_THRESHOLD = 800;
    private static final int POINTS_PER_DISCOUNT_PCT  = 100; // 100 pts = 1%

    @Autowired private LoyaltyRepository             loyaltyRepo;
    @Autowired private LoyaltyTransactionRepository  txRepo;
    @Autowired private OrderRepository               orderRepo;
    @Autowired private MachineRepository             machineRepo;
    @Autowired private ServiceRepository             serviceRepo;
    @Autowired private NotificationService           notificationService;

    // ✅ NOUVEAU : injection du service PromoCode
    @Autowired private PromoCodeService promoCodeService;

    @Autowired private ServiceRequestRepository serviceRequestRepo;

    // ═════════════════════════════════════════════════════════
    // COMPTE FIDÉLITÉ
    // ═════════════════════════════════════════════════════════

    /** Crée ou retourne le compte fidélité d'un utilisateur */
    public LoyaltyAccount getOrCreateAccount(Long userId, String userName) {
        return loyaltyRepo.findByUserId(userId).orElseGet(() -> {
            LoyaltyAccount acc = new LoyaltyAccount();
            acc.setUserId(userId);
            acc.setUserName(userName);
            return loyaltyRepo.save(acc);
        });
    }

    /** DTO complet pour l'affichage dans l'UI */
    public LoyaltyAccountDTO getAccountDTO(Long userId) {
        LoyaltyAccount acc = loyaltyRepo.findByUserId(userId)
                .orElseGet(() -> getOrCreateAccount(userId, "Utilisateur"));
        List<LoyaltyTransaction> history = txRepo.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        return toDTO(acc, history);
    }

    // ═════════════════════════════════════════════════════════
    // GAIN DE POINTS (à appeler après chaque commande validée)
    // ═════════════════════════════════════════════════════════

    /**
     * Crédite les points pour une commande.
     * Appelé depuis OrderServiceImpl.updateStatus() quand status → CONFIRMED
     */
    public void earnPointsForOrder(Long userId, String userName, Long orderId,
                                   double orderAmount) {
        LoyaltyAccount acc = getOrCreateAccount(userId, userName);

        // Calcul des points avec bonus palier
        double multiplier = getTierMultiplier(acc.getTier());
        int pts = (int) Math.floor(orderAmount * BASE_POINTS_PER_TND * multiplier);
        if (pts <= 0) return;

        // Bonus premier achat
        boolean isFirst = txRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().noneMatch(t -> "EARN_ORDER".equals(t.getType()));
        String type = isFirst ? "EARN_FIRST_ORDER" : "EARN_ORDER";
        int bonusFirst = isFirst ? 100 : 0;
        int totalPts = pts + bonusFirst;

        // Mettre à jour le compte
        String tierBefore = acc.getTier();
        boolean freeDelivBefore  = Boolean.TRUE.equals(acc.getFreeDeliveryAvailable());
        boolean premiumBefore    = Boolean.TRUE.equals(acc.getPremiumAccessAvailable());

        acc.setPoints(acc.getPoints() + totalPts);
        acc.setTotalPointsEarned(acc.getTotalPointsEarned() + totalPts);
        acc.recalculateTier();
        acc.recalculateRewards();
        loyaltyRepo.save(acc);

        // Enregistrer la transaction
        String desc = isFirst
                ? String.format("🎉 Bienvenue ! Premier achat + %d pts bonus (commande #%d)", bonusFirst, orderId)
                : String.format("🛒 Commande #%d — %.0f TND × %.1f = %d pts", orderId, orderAmount, multiplier, pts);
        saveTransaction(userId, type, totalPts, acc.getPoints(), desc, orderId);

        // ── NOTIFICATIONS DE RÉCOMPENSES ──────────────────────
        // Nouveau palier ?
        if (!tierBefore.equals(acc.getTier())) {
            sendTierUpNotification(userId, acc.getTier(), acc.getPoints());
        }
        // Livraison gratuite débloquée ?
        if (!freeDelivBefore && Boolean.TRUE.equals(acc.getFreeDeliveryAvailable())) {
            notificationService.create(userId,
                    NotificationType.LOYALTY_FREE_DELIVERY,
                    "🚚 Livraison gratuite débloquée !",
                    "Vous avez " + acc.getPoints() + " pts de fidélité. "
                            + "Votre prochaine commande bénéficie d'une livraison gratuite !",
                    null, "LOYALTY", "/loyalty");
        }
        // Accès premium débloqué ?
        if (!premiumBefore && Boolean.TRUE.equals(acc.getPremiumAccessAvailable())) {
            notificationService.create(userId,
                    NotificationType.LOYALTY_PREMIUM_ACCESS,
                    "🎁 Accès Premium débloqué !",
                    "Félicitations ! Vos " + acc.getPoints() + " pts vous donnent accès "
                            + "aux fournisseurs premium de la plateforme.",
                    null, "LOYALTY", "/loyalty");
        }
        // Notification normale d'accumulation
        notificationService.create(userId,
                NotificationType.LOYALTY_POINTS_EARNED,
                "⭐ +" + totalPts + " points de fidélité",
                desc + (isFirst ? "" : " (total : " + acc.getPoints() + " pts)"),
                orderId, "ORDER", "/loyalty");
    }

    // ═════════════════════════════════════════════════════════
    // UTILISATION DES POINTS (rachats / rédemptions)
    // ✅ VERSION CORRIGÉE : retourne un PromoCodeDTO au lieu de boolean/int
    // ═════════════════════════════════════════════════════════

    /**
     * Utilise des points pour une réduction
     * @return PromoCodeDTO contenant le code promo généré
     */
    public PromoCodeDTO redeemDiscount(Long userId, int pointsToUse) {
        LoyaltyAccount acc = loyaltyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité introuvable"));
        if (acc.getPoints() < pointsToUse)
            throw new RuntimeException("Points insuffisants");

        int discountPct = Math.min(15, pointsToUse / POINTS_PER_DISCOUNT_PCT);

        acc.setPoints(acc.getPoints() - pointsToUse);
        acc.setTotalPointsSpent(acc.getTotalPointsSpent() + pointsToUse);
        acc.recalculateRewards();
        loyaltyRepo.save(acc);

        saveTransaction(userId, "REDEEM_DISCOUNT", -pointsToUse, acc.getPoints(),
                String.format("💸 Réduction de %d%% — code promo généré", discountPct), null);

        // ✅ Générer un code promo
        PromoCode code = promoCodeService.generateDiscountCode(userId, discountPct, pointsToUse);

        notificationService.create(userId,
                NotificationType.LOYALTY_POINTS_SPENT,
                "💸 Code promo de -" + discountPct + "% généré !",
                "Votre code : **" + code.getCode() + "**. "
                        + pointsToUse + " points déduits. Valide 7 jours.",
                null, "LOYALTY", "/loyalty");

        return toPromoDTO(code);
    }

    /**
     * Utilise des points pour la livraison gratuite
     * @return PromoCodeDTO contenant le code promo généré
     */
    public PromoCodeDTO redeemFreeDelivery(Long userId) {
        LoyaltyAccount acc = loyaltyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité introuvable"));
        if (acc.getPoints() < FREE_DELIVERY_THRESHOLD)
            throw new RuntimeException("Points insuffisants (300 pts requis)");

        acc.setPoints(acc.getPoints() - FREE_DELIVERY_THRESHOLD);
        acc.setTotalPointsSpent(acc.getTotalPointsSpent() + FREE_DELIVERY_THRESHOLD);
        acc.recalculateRewards();
        loyaltyRepo.save(acc);

        saveTransaction(userId, "REDEEM_DELIVERY", -FREE_DELIVERY_THRESHOLD, acc.getPoints(),
                "🚚 Livraison gratuite — code promo généré", null);

        // ✅ Générer un code promo
        PromoCode code = promoCodeService.generateFreeDeliveryCode(userId);

        notificationService.create(userId,
                NotificationType.LOYALTY_FREE_DELIVERY,
                "🚚 Code livraison gratuite généré !",
                "Votre code : **" + code.getCode() + "**. "
                        + "300 points déduits. Valide 7 jours.",
                null, "LOYALTY", "/loyalty");

        return toPromoDTO(code);
    }

    /**
     * Utilise des points pour l'accès premium
     * @return PromoCodeDTO contenant le code promo généré
     */
    public PromoCodeDTO redeemPremiumAccess(Long userId) {
        LoyaltyAccount acc = loyaltyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité introuvable"));
        if (acc.getPoints() < PREMIUM_ACCESS_THRESHOLD)
            throw new RuntimeException("Points insuffisants (800 pts requis)");

        acc.setPoints(acc.getPoints() - PREMIUM_ACCESS_THRESHOLD);
        acc.setTotalPointsSpent(acc.getTotalPointsSpent() + PREMIUM_ACCESS_THRESHOLD);
        acc.recalculateRewards();
        loyaltyRepo.save(acc);

        saveTransaction(userId, "REDEEM_PREMIUM", -PREMIUM_ACCESS_THRESHOLD, acc.getPoints(),
                "🎁 Accès Premium — code promo généré", null);

        // ✅ Générer un code promo
        PromoCode code = promoCodeService.generatePremiumCode(userId);

        notificationService.create(userId,
                NotificationType.LOYALTY_PREMIUM_ACCESS,
                "🎁 Code Premium généré !",
                "Votre code : **" + code.getCode() + "**. "
                        + "800 points déduits. Valide 7 jours.",
                null, "LOYALTY", "/loyalty");

        return toPromoDTO(code);
    }

    // ═════════════════════════════════════════════════════════
    // TOP MACHINES & SERVICES (par volume de commandes)
    // ═════════════════════════════════════════════════════════

    /**
     * Retourne les N machines les plus commandées avec leur rang.
     * @param limit  nombre max de résultats (5, 10, 20)
     * @param category filtre catégorie optionnel
     */
    public List<TopItemDTO> getTopMachines(int limit, String category) {
        // Récupère toutes les commandes livrées/confirmées
        List<Order> orders = orderRepo.findByStatusIn(
                Arrays.asList("DELIVERED", "CONFIRMED", "IN_DELIVERY")
        );

        // Comptage par itemId
        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, Double> revenueMap = new HashMap<>();

        for (Order order : orders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                countMap.merge(item.getMachineId(), (long) item.getQuantity(), Long::sum);
                revenueMap.merge(item.getMachineId(),
                        item.getTotalPrice() != null ? item.getTotalPrice() : 0.0, Double::sum);
            }
        }

        // Récupérer les machines approuvées
        List<Machine> machines = machineRepo.findByValidationStatus("APPROVED");
        if (category != null && !category.isEmpty()) {
            machines = machines.stream()
                    .filter(m -> m.getCategory() != null && category.equalsIgnoreCase(m.getCategory().name()))
                    .collect(Collectors.toList());
        }

        // Scorer et trier
        List<TopItemDTO> result = machines.stream().map(m -> {
                    long sold = countMap.getOrDefault(m.getId(), 0L);
                    double revenue = revenueMap.getOrDefault(m.getId(), 0.0);
                    return buildTopItemDTO(m, sold, revenue);
                })
                .sorted(Comparator.comparingLong(TopItemDTO::getTotalSold).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Assigner les rangs
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
            result.get(i).setRankLabel(getRankLabel(i + 1));
        }

        return result;
    }

    /**
     * Retourne les N services les plus commandés.
     */
    public List<TopItemDTO> getTopServices(int limit, String category) {
        // Les services passent par les demandes (ServiceRequest avec status COMPLETED)
        List<ServiceRequest> completed = serviceRequestRepo.findByStatus(RequestStatus.COMPLETED);
        Map<Long, Long> countMap   = new HashMap<>();
        Map<Long, Double> revenueMap = new HashMap<>();
        for (ServiceRequest req : completed) {
            if (req.getMachineServiceId() == null) continue;
            countMap.merge(req.getMachineServiceId(), 1L, Long::sum);
            revenueMap.merge(req.getMachineServiceId(),
                    req.getProposedPrice() != null ? req.getProposedPrice() : 0.0, Double::sum);
        }

        List<ServiceEntity> services = serviceRepo.findByValidationStatus("APPROVED");
        if (category != null && !category.isEmpty()) {
            services = services.stream()
                    .filter(s -> s.getCategory() != null && category.equalsIgnoreCase(s.getCategory().name()))
                    .collect(Collectors.toList());
        }

        List<TopItemDTO> result = services.stream().map(s -> {
                    long sold = countMap.getOrDefault(s.getId(), 0L);
                    double rev = revenueMap.getOrDefault(s.getId(), 0.0);
                    return buildTopServiceDTO(s, sold, rev);
                })
                .sorted(Comparator.comparingLong(TopItemDTO::getTotalSold).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
            result.get(i).setRankLabel(getRankLabel(i + 1));
        }

        return result;
    }

    // ═════════════════════════════════════════════════════════
    // UTILITAIRES PRIVÉS
    // ═════════════════════════════════════════════════════════

    private double getTierMultiplier(String tier) {
        switch (tier) {
            case "PLATINUM": return 2.0;
            case "GOLD":     return 1.5;
            case "SILVER":   return 1.2;
            default:         return 1.0;
        }
    }

    private void sendTierUpNotification(Long userId, String newTier, int points) {
        String emoji = switch (newTier) {
            case "SILVER"   -> "🥈";
            case "GOLD"     -> "🥇";
            case "PLATINUM" -> "💎";
            default         -> "⭐";
        };
        notificationService.create(userId,
                NotificationType.LOYALTY_TIER_UP,
                emoji + " Nouveau palier : " + newTier + " !",
                "Félicitations ! Vous atteignez le palier " + newTier +
                        " avec " + points + " points. De nouveaux avantages sont disponibles !",
                null, "LOYALTY", "/loyalty");
    }

    private void saveTransaction(Long userId, String type, int delta,
                                 int balanceAfter, String description, Long orderId) {
        LoyaltyTransaction tx = new LoyaltyTransaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setPointsDelta(delta);
        tx.setBalanceAfter(balanceAfter);
        tx.setDescription(description);
        tx.setOrderId(orderId);
        txRepo.save(tx);
    }

    private TopItemDTO buildTopItemDTO(Machine m, long sold, double revenue) {
        TopItemDTO dto = new TopItemDTO();
        dto.setId(m.getId());
        dto.setResourceType("MACHINE");
        dto.setName(m.getName());
        dto.setCategory(m.getCategory() != null ? m.getCategory().name() : null);
        dto.setImageUrl(m.getImageUrls() != null && !m.getImageUrls().isEmpty() ? m.getImageUrls().get(0) : null);
        dto.setPrice(m.getPrice());
        dto.setPriceUnit(m.getPriceUnit());
        dto.setRating(m.getRating());
        dto.setReviewCount(m.getReviewCount());
        dto.setLocation(m.getLocation());
        dto.setSupplierName(m.getSupplierName());
        dto.setTotalSold(sold);
        dto.setTotalRevenue(revenue);
        dto.setAvailability(m.getAvailability() != null ? m.getAvailability().name() : "UNKNOWN");
        return dto;
    }

    private TopItemDTO buildTopServiceDTO(ServiceEntity s, long sold, double revenue) {
        TopItemDTO dto = new TopItemDTO();
        dto.setId(s.getId());
        dto.setResourceType("SERVICE");
        dto.setName(s.getName());
        dto.setCategory(s.getCategory() != null ? s.getCategory().name() : null);
        dto.setImageUrl(s.getImageUrls() != null && !s.getImageUrls().isEmpty() ? s.getImageUrls().get(0) : null);
        dto.setPrice(s.getBasePrice());
        dto.setPriceUnit(s.getPriceUnit());
        dto.setRating(s.getRating());
        dto.setReviewCount(s.getReviewCount());
        dto.setLocation(s.getLocation());
        dto.setSupplierName(s.getProviderName());
        dto.setTotalSold(sold);
        dto.setTotalRevenue(revenue);
        dto.setAvailability(s.getAvailability() != null ? s.getAvailability().name() : "UNKNOWN");
        return dto;
    }

    private String getRankLabel(int rank) {
        switch (rank) {
            case 1:  return "🥇 N°1";
            case 2:  return "🥈 N°2";
            case 3:  return "🥉 N°3";
            default: return "Top " + rank;
        }
    }

    private LoyaltyAccountDTO toDTO(LoyaltyAccount acc, List<LoyaltyTransaction> history) {
        LoyaltyAccountDTO dto = new LoyaltyAccountDTO();
        dto.setUserId(acc.getUserId());
        dto.setUserName(acc.getUserName());
        dto.setPoints(acc.getPoints());
        dto.setTotalPointsEarned(acc.getTotalPointsEarned());
        dto.setTotalPointsSpent(acc.getTotalPointsSpent());
        dto.setTier(acc.getTier());
        dto.setFreeDeliveryAvailable(acc.getFreeDeliveryAvailable());
        dto.setPremiumAccessAvailable(acc.getPremiumAccessAvailable());
        dto.setDiscountPercent(acc.getDiscountPercent());

        // Prochain palier
        int[] thresholds = {500, 1500, 4000};
        String[] tiers = {"SILVER", "GOLD", "PLATINUM"};
        int earned = acc.getTotalPointsEarned();
        for (int i = 0; i < thresholds.length; i++) {
            if (earned < thresholds[i]) {
                dto.setNextTier(tiers[i]);
                dto.setPointsToNextTier(thresholds[i] - earned);
                dto.setProgressPercent((int)((earned * 100.0) / thresholds[i]));
                break;
            }
        }
        if (dto.getNextTier() == null) {
            dto.setNextTier("PLATINUM");
            dto.setPointsToNextTier(0);
            dto.setProgressPercent(100);
        }

        dto.setHistory(history.stream().map(tx -> {
            LoyaltyAccountDTO.TxDTO t = new LoyaltyAccountDTO.TxDTO();
            t.setId(tx.getId());
            t.setType(tx.getType());
            t.setPointsDelta(tx.getPointsDelta());
            t.setBalanceAfter(tx.getBalanceAfter());
            t.setDescription(tx.getDescription());
            t.setCreatedAt(tx.getCreatedAt());
            return t;
        }).collect(Collectors.toList()));

        return dto;
    }

    // ✅ Méthode helper pour convertir PromoCode en DTO
    // Dans LoyaltyService.java, corriger la méthode toPromoDTO() :

    private PromoCodeDTO toPromoDTO(PromoCode code) {
        PromoCodeDTO dto = new PromoCodeDTO();
        dto.setId(code.getId());                                    // ✅ setter correct
        dto.setCode(code.getCode());
        dto.setType(code.getType());
        dto.setDiscountPercent(code.getDiscountPercent());
        dto.setExpiresAt(code.getExpiresAt());                      // ✅ expiresAt, pas validUntil
        dto.setUsed(code.isUsed());                                 // ✅ isUsed() au lieu de getUsed()
        dto.setPointsCost(code.getPointsCost());

        // Message personnalisé selon le type
        String message;
        switch (code.getType()) {
            case "FREE_DELIVERY":
                message = "🚚 Livraison gratuite sur votre prochaine commande !";
                break;
            case "DISCOUNT":
                message = "💸 Réduction de " + code.getDiscountPercent() + "% sur votre prochain achat !";
                break;
            case "PREMIUM":
                message = "🎁 Accès Premium aux fournisseurs pendant 30 jours !";
                break;
            default:
                message = "Code promo fidélité";
                break;
        }
        dto.setMessage(message);

        return dto;
    }
}