package org.example.backend_pi.controller;

// controller/LoyaltyController.java
// Endpoints fidélité + top machines/services
// ══════════════════════════════════════════════════════════════
// ✅ MIS À JOUR : Les méthodes redeem retournent maintenant
//    PromoCodeDTO (JSON) au lieu de Map<String, Object>
// ══════════════════════════════════════════════════════════════

import org.example.backend_pi.dto.LoyaltyAccountDTO;
import org.example.backend_pi.dto.PromoCodeDTO;
import org.example.backend_pi.dto.TopItemDTO;
import org.example.backend_pi.service.LoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    // ─── COMPTE FIDÉLITÉ ──────────────────────────────────────

    /** GET /api/loyalty/account/{userId} → Solde, palier, historique */
    @GetMapping("/account/{userId}")
    public ResponseEntity<LoyaltyAccountDTO> getAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getAccountDTO(userId));
    }

    /** POST /api/loyalty/account/{userId}/init → Créer le compte si inexistant */
    @PostMapping("/account/{userId}/init")
    public ResponseEntity<LoyaltyAccountDTO> initAccount(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String name = body.getOrDefault("userName", "Utilisateur");
        loyaltyService.getOrCreateAccount(userId, name);
        return ResponseEntity.ok(loyaltyService.getAccountDTO(userId));
    }

    // ─── UTILISATION DES POINTS ───────────────────────────────
    // ✅ VERSION CORRIGÉE : retourne PromoCodeDTO

    /**
     * POST /api/loyalty/account/{userId}/redeem/discount
     * Body: { "points": 200 }
     * → Utilise N points pour une réduction
     * Retourne : PromoCodeDTO avec le code promo généré
     */
    @PostMapping("/account/{userId}/redeem/discount")
    public ResponseEntity<PromoCodeDTO> redeemDiscount(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> body) {
        try {
            int pts = body.getOrDefault("points", 0);
            if (pts <= 0) {
                return ResponseEntity.badRequest().build();
            }
            PromoCodeDTO code = loyaltyService.redeemDiscount(userId, pts);
            return ResponseEntity.ok(code);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/loyalty/account/{userId}/redeem/delivery
     * → Utilise 300 pts pour la livraison gratuite
     * Retourne : PromoCodeDTO avec le code promo généré
     */
    @PostMapping("/account/{userId}/redeem/delivery")
    public ResponseEntity<PromoCodeDTO> redeemDelivery(@PathVariable Long userId) {
        try {
            PromoCodeDTO code = loyaltyService.redeemFreeDelivery(userId);
            return ResponseEntity.ok(code);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/loyalty/account/{userId}/redeem/premium
     * → Utilise 800 pts pour l'accès premium
     * Retourne : PromoCodeDTO avec le code promo généré
     */
    @PostMapping("/account/{userId}/redeem/premium")
    public ResponseEntity<PromoCodeDTO> redeemPremium(@PathVariable Long userId) {
        try {
            PromoCodeDTO code = loyaltyService.redeemPremiumAccess(userId);
            return ResponseEntity.ok(code);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── TOP CLASSEMENTS ─────────────────────────────────────

    /**
     * GET /api/loyalty/top/machines?limit=10&category=INDUSTRIELLE
     * → Top N machines les plus commandées
     */
    @GetMapping("/top/machines")
    public ResponseEntity<List<TopItemDTO>> getTopMachines(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(loyaltyService.getTopMachines(limit, category));
    }

    /**
     * GET /api/loyalty/top/services?limit=10&category=INDUSTRIELLE
     * → Top N services les plus commandés
     */
    @GetMapping("/top/services")
    public ResponseEntity<List<TopItemDTO>> getTopServices(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(loyaltyService.getTopServices(limit, category));
    }
}