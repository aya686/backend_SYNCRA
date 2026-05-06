package org.example.backend_pi.controller;

// controller/CartController.java
// ══════════════════════════════════════════════════════════════
// ✅ BUG CORRIGÉ : checkout() retournait ResponseEntity<String>
//    (texte brut) → Angular essayait de le parser en JSON → erreur
//    "[object Object]" côté frontend même si la commande réussissait.
//    CORRECTION : retourner Map<String,Object> (JSON valide).
//
// ✅ NOUVEAU : endpoint applique un code promo avant checkout
// ══════════════════════════════════════════════════════════════

import org.example.backend_pi.dto.AddToCartDTO;
import org.example.backend_pi.dto.DeliveryInfoDTO;
import org.example.backend_pi.entity.Cart;
import org.example.backend_pi.service.CartService;
import org.example.backend_pi.service.PromoCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private PromoCodeService promoCodeService;

    // ─── PANIER ──────────────────────────────────────────────────

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Long userId,
                                       @RequestBody AddToCartDTO dto) {
        try {
            Cart cart = cartService.addToCart(userId, dto);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/remove/{cartItemId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long userId,
                                               @PathVariable Long cartItemId) {
        Cart cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * ✅ BUG CORRIGÉ : Angular envoie { quantity: N } dans le body JSON,
     * l'ancien code utilisait @RequestParam qui attend ?quantity=N dans l'URL.
     * Correction : on lit la quantité depuis le @RequestBody.
     */
    @PutMapping("/{userId}/update/{cartItemId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long userId,
                                            @PathVariable Long cartItemId,
                                            @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");
            if (quantity == null) {
                return ResponseEntity.badRequest().body("Le champ 'quantity' est requis dans le body");
            }
            Cart cart = cartService.updateQuantity(userId, cartItemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable Long userId) {
        Cart cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{userId}/delivery")
    public ResponseEntity<Cart> updateDeliveryInfo(@PathVariable Long userId,
                                                   @RequestBody DeliveryInfoDTO dto) {
        Cart cart = cartService.updateDeliveryInfo(userId, dto);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/delivery-cost")
    public ResponseEntity<Cart> calculateDeliveryCost(@PathVariable Long userId,
                                                      @RequestParam String deliveryType) {
        Cart cart = cartService.calculateDeliveryCost(userId, deliveryType);
        return ResponseEntity.ok(cart);
    }

    // ─── CODE PROMO ──────────────────────────────────────────────

    /**
     * POST /api/cart/{userId}/apply-promo
     * Body : { "code": "LIVRAISON-XXXX" }
     *
     * Retourne :
     * {
     *   "valid": true,
     *   "type": "FREE_DELIVERY" | "DISCOUNT",
     *   "discountPercent": 10,
     *   "message": "Livraison gratuite appliquée !",
     *   "code": "LIVRAISON-XXXX"
     * }
     */
    @PostMapping("/{userId}/apply-promo")
    public ResponseEntity<Map<String, Object>> applyPromoCode(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");
            if (code == null || code.isBlank()) {
                Map<String, Object> error = new HashMap<>();
                error.put("valid", false);
                error.put("message", "Veuillez saisir un code promo");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> result = promoCodeService.validateAndApply(userId, code.toUpperCase().trim());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─── CHECKOUT ────────────────────────────────────────────────

    /**
     * POST /api/cart/{userId}/checkout
     * Body (optionnel) : { "promoCode": "LIVRAISON-XXXX" }
     *
     * ✅ BUG CORRIGÉ : retourne maintenant Map<String,Object> (JSON valide)
     * au lieu de ResponseEntity<String> (texte brut non parseable par Angular).
     *
     * Retourne :
     * {
     *   "success": true,
     *   "message": "Commande validée avec succès !",
     *   "orderNumber": "ORD-2026-00042",
     *   "pointsEarned": 450
     * }
     */
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String promoCode = (body != null) ? body.get("promoCode") : null;

            // Appliquer le code promo sur le panier avant checkout
            if (promoCode != null && !promoCode.isBlank()) {
                promoCodeService.applyToCart(userId, promoCode.toUpperCase().trim());
            }

            // Effectuer le checkout → crée la commande en base
            String orderNumber = cartService.checkout(userId);

            // Calculer les points qui seront gagnés (estimatif)
            int pointsEarned = promoCodeService.estimatePoints(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Commande validée avec succès !");
            response.put("orderNumber", orderNumber != null ? orderNumber : "");
            response.put("pointsEarned", pointsEarned);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}