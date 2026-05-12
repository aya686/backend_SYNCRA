package org.example.backend_pi.service;
// service/PromoCodeService.java
// ══════════════════════════════════════════════════════════════
// MOTEUR DE CODES PROMO FIDÉLITÉ
//
// Flux complet :
// 1. L'utilisateur clique "Utiliser ma récompense" dans /loyalty
// 2. → LoyaltyService génère un code promo et déduit les points
// 3. Le code s'affiche à l'utilisateur (ex: LIVRAISON-A7B3)
// 4. L'utilisateur colle le code dans le panier avant checkout
// 5. → PromoCodeService.validateAndApply() vérifie et applique la remise
// 6. → checkout() est appelé avec le panier modifié
// ══════════════════════════════════════════════════════════════

import org.example.backend_pi.entity.Cart;
import org.example.backend_pi.entity.PromoCode;
import org.example.backend_pi.repository.CartRepository;
import org.example.backend_pi.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PromoCodeService {

    @Autowired private PromoCodeRepository promoRepo;
    @Autowired private CartRepository      cartRepo;

    // ─── GÉNÉRATION D'UN CODE ─────────────────────────────────

    /**
     * Génère un code promo pour livraison gratuite.
     * Appelé par LoyaltyService.redeemFreeDelivery()
     */
    public PromoCode generateFreeDeliveryCode(Long userId) {
        PromoCode code = new PromoCode();
        code.setUserId(userId);
        code.setCode("LIVRAISON-" + generateSuffix());
        code.setType("FREE_DELIVERY");
        code.setDiscountPercent(0);
        code.setPointsCost(300);
        return promoRepo.save(code);
    }

    /**
     * Génère un code promo pour une réduction en %.
     * Appelé par LoyaltyService.redeemDiscount()
     * @param discountPercent % de réduction (ex: 10)
     * @param pointsUsed nombre de points déduits
     */
    public PromoCode generateDiscountCode(Long userId, int discountPercent, int pointsUsed) {
        PromoCode code = new PromoCode();
        code.setUserId(userId);
        code.setCode("REDUC" + discountPercent + "-" + generateSuffix());
        code.setType("DISCOUNT");
        code.setDiscountPercent(discountPercent);
        code.setPointsCost(pointsUsed);
        return promoRepo.save(code);
    }

    /**
     * Génère un code promo pour l'accès premium.
     * Appelé par LoyaltyService.redeemPremiumAccess()
     */
    public PromoCode generatePremiumCode(Long userId) {
        PromoCode code = new PromoCode();
        code.setUserId(userId);
        code.setCode("PREMIUM-" + generateSuffix());
        code.setType("PREMIUM");
        code.setDiscountPercent(0);
        code.setPointsCost(800);
        return promoRepo.save(code);
    }

    // ─── VALIDATION ───────────────────────────────────────────

    /**
     * Valide un code promo (sans l'appliquer au panier).
     * Appelé par le bouton "Vérifier" dans le frontend.
     *
     * @return Map avec : valid, type, discountPercent, message
     */
    public Map<String, Object> validateAndApply(Long userId, String codeStr) {
        Map<String, Object> result = new HashMap<>();

        PromoCode code = promoRepo.findByCode(codeStr)
                .orElse(null);

        if (code == null) {
            result.put("valid",   false);
            result.put("message", "Code promo invalide ou inexistant");
            return result;
        }

        if (!code.getUserId().equals(userId)) {
            result.put("valid",   false);
            result.put("message", "Ce code promo ne vous appartient pas");
            return result;
        }

        if (!code.isValid()) {
            String reason = Boolean.TRUE.equals(code.getUsed())
                    ? "Ce code promo a déjà été utilisé"
                    : "Ce code promo a expiré";
            result.put("valid",   false);
            result.put("message", reason);
            return result;
        }

        // Code valide
        result.put("valid",           true);
        result.put("code",            code.getCode());
        result.put("type",            code.getType());
        result.put("discountPercent", code.getDiscountPercent());
        result.put("expiresAt",       code.getExpiresAt().toString());

        switch (code.getType()) {
            case "FREE_DELIVERY":
                result.put("message", "🚚 Livraison gratuite ! Ce code supprimera les frais de livraison.");
                break;
            case "DISCOUNT":
                result.put("message", "💸 Réduction de " + code.getDiscountPercent()
                        + "% sur le total de votre commande !");
                break;
            case "PREMIUM":
                result.put("message", "🎁 Accès premium activé !");
                break;
            default:
                result.put("message", "Code promo valide ✅");
        }

        return result;
    }

    /**
     * Applique le code promo au panier (modifie deliveryCost ou totalAmount).
     * Marque le code comme utilisé.
     * Appelé depuis CartController.checkout()
     */
    public void applyToCart(Long userId, String codeStr) {
        PromoCode code = promoRepo.findByCode(codeStr)
                .orElseThrow(() -> new RuntimeException("Code promo invalide : " + codeStr));

        if (!code.getUserId().equals(userId))
            throw new RuntimeException("Ce code promo ne vous appartient pas");

        if (!code.isValid())
            throw new RuntimeException(Boolean.TRUE.equals(code.getUsed())
                    ? "Ce code promo a déjà été utilisé"
                    : "Ce code promo a expiré");

        // Récupérer le panier
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable"));

        switch (code.getType()) {
            case "FREE_DELIVERY":
                // Annuler les frais de livraison
                cart.setDeliveryCost(0.0);
                cart.calculateTotal();
                cartRepo.save(cart);
                break;

            case "DISCOUNT":
                // Appliquer le % de réduction sur le sous-total
                double subtotal = cart.getItems().stream()
                        .mapToDouble(i -> i.getTotalPrice() != null ? i.getTotalPrice() : 0)
                        .sum();
                double discount = subtotal * code.getDiscountPercent() / 100.0;
                // Stocker la remise comme livraison négative (pour simplifier sans changer la DB)
                // → on réduit le totalAmount directement
                double newTotal = (subtotal - discount) + (cart.getDeliveryCost() != null ? cart.getDeliveryCost() : 0);
                cart.setTotalAmount(Math.max(0, newTotal));
                cartRepo.save(cart);
                break;

            case "PREMIUM":
                // Pas de modification du prix — l'accès premium est géré par le compte
                break;
        }

        // Marquer le code comme utilisé
        code.setUsed(true);
        code.setUsedAt(LocalDateTime.now());
        promoRepo.save(code);
    }

    // ─── LISTE DES CODES ACTIFS ───────────────────────────────

    /**
     * Retourne tous les codes promo actifs d'un utilisateur.
     */
    public List<PromoCode> getActiveCodes(Long userId) {
        return promoRepo.findByUserIdAndUsedFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Retourne tout l'historique des codes d'un utilisateur.
     */
    public List<PromoCode> getAllCodes(Long userId) {
        return promoRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── UTILITAIRE POUR CHECKOUT ─────────────────────────────

    /**
     * Estime les points qui seront gagnés lors du checkout.
     * Utilisé uniquement pour l'affichage dans le message de succès.
     */
    public int estimatePoints(Long userId) {
        Cart cart = cartRepo.findByUserId(userId).orElse(null);
        if (cart == null || cart.getTotalAmount() == null) return 0;
        return (int) Math.floor(cart.getTotalAmount()); // 1 TND = 1 point (Bronze)
    }

    // ─── PRIVÉ ────────────────────────────────────────────────

    /** Génère un suffixe aléatoire à 4 caractères majuscules/chiffres */
    private String generateSuffix() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans I, O, 0, 1
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}