package org.example.backend_pi.service;

// service/OrderService.java + impl combined

import org.example.backend_pi.entity.*;
import org.example.backend_pi.enums.OrderStatus;

import java.util.List;

// ─── INTERFACE ───────────────────────────────────────────────
public interface OrderService {
    Order createFromCart(Cart cart);
    Order getById(Long id);
    Order getByOrderNumber(String orderNumber);
    List<Order> getAllOrders();
    List<Order> getByUserId(Long userId);
    List<Order> getByStatus(OrderStatus status);
    Order updateStatus(Long orderId, OrderStatus status, String adminNote);
    void deleteOrder(Long orderId);
}

