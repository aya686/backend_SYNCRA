package org.example.backend_pi.controller;

// controller/CartController.java


import org.example.backend_pi.entity.Cart;
import org.example.backend_pi.service.CartService;
import org.example.backend_pi.dto.AddToCartDTO;
import org.example.backend_pi.dto.DeliveryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Récupérer le panier d'un utilisateur
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    // Ajouter un item au panier
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Long userId, @RequestBody AddToCartDTO addToCartDTO) {
        try {
            Cart cart = cartService.addToCart(userId, addToCartDTO);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            // Retourner le message d'erreur pour plus de clarté
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Supprimer un item du panier
    @DeleteMapping("/{userId}/remove/{cartItemId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long userId, @PathVariable Long cartItemId) {
        Cart cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    // Modifier la quantité d'un item
    @PutMapping("/{userId}/update/{cartItemId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable Long userId,
                                               @PathVariable Long cartItemId,
                                               @RequestParam Integer quantity) {
        Cart cart = cartService.updateQuantity(userId, cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }

    // Vider le panier
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable Long userId) {
        Cart cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    // Mettre à jour les informations de livraison
    @PutMapping("/{userId}/delivery")
    public ResponseEntity<Cart> updateDeliveryInfo(@PathVariable Long userId,
                                                   @RequestBody DeliveryInfoDTO deliveryInfoDTO) {
        Cart cart = cartService.updateDeliveryInfo(userId, deliveryInfoDTO);
        return ResponseEntity.ok(cart);
    }

    // Calculer les frais de livraison
    @PostMapping("/{userId}/delivery-cost")
    public ResponseEntity<Cart> calculateDeliveryCost(@PathVariable Long userId,
                                                      @RequestParam String deliveryType) {
        Cart cart = cartService.calculateDeliveryCost(userId, deliveryType);
        return ResponseEntity.ok(cart);
    }

    // Valider la commande (checkout)
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkout(@PathVariable Long userId) {
        try {
            cartService.checkout(userId);
            return ResponseEntity.ok("Commande validée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}