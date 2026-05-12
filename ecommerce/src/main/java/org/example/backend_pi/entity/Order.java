package org.example.backend_pi.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.backend_pi.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, length = 30)
    private String orderNumber;   // Ex: ORD-2026-00001

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    // Livraison
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;

    @Column(name = "delivery_zip_code", length = 20)
    private String deliveryZipCode;

    @Column(name = "delivery_phone", length = 20)
    private String deliveryPhone;

    @Column(name = "delivery_type", length = 30)
    private String deliveryType;

    @Column(name = "delivery_cost")
    private Double deliveryCost = 0.0;

    // Prix
    @Column(name = "subtotal")
    private Double subtotal = 0.0;

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    // Statut
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}