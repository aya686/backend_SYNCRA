package org.example.backend_pi.enums;

// enums/OrderStatus.java

public enum OrderStatus {
    PENDING,        // En attente de confirmation admin
    CONFIRMED,      // Confirmée par l'admin
    IN_DELIVERY,    // En cours de livraison
    DELIVERED,      // Livrée
    CANCELLED,      // Annulée
    REFUNDED        // Remboursée
}
