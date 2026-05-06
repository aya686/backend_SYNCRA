package org.example.backend_pi.controller;

// controller/OrderController.java

import org.example.backend_pi.entity.Order;
import org.example.backend_pi.enums.OrderStatus;

import org.example.backend_pi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ─── VUE ADMIN ───────────────────────────────────────────────

    /** Toutes les commandes (admin seulement) */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /** Commandes par statut */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable String status) {
        try {
            OrderStatus s = OrderStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(orderService.getByStatus(s));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── VUE UTILISATEUR ─────────────────────────────────────────

    /** Commandes d'un utilisateur spécifique */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getByUserId(userId));
    }

    /** Détail d'une commande */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(order);
    }

    // ─── ACTIONS ADMIN ───────────────────────────────────────────

    /**
     * Changer le statut d'une commande (admin).
     * Body: { "status": "CONFIRMED", "adminNote": "..." }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            OrderStatus status = OrderStatus.valueOf(payload.get("status").toUpperCase());
            String note = payload.getOrDefault("adminNote", null);
            Order order = orderService.updateStatus(id, status, note);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Supprimer une commande (admin) */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("Commande supprimée");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}