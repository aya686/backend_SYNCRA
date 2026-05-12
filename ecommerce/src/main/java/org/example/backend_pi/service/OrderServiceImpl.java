package org.example.backend_pi.service;

import org.example.backend_pi.entity.Cart;
import org.example.backend_pi.entity.CartItem;
import org.example.backend_pi.entity.Order;
import org.example.backend_pi.entity.OrderItem;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.enums.OrderStatus;
import org.example.backend_pi.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Override
    public Order createFromCart(Cart cart) {
        Order order = new Order();

        // ✅ Générer un numéro de commande unique basé sur timestamp
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = timestamp.substring(timestamp.length() - 6);
        String orderNumber = "ORD-" + year + "-" + uniqueId;

        // Vérifier l'unicité
        int counter = 1;
        while (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            orderNumber = "ORD-" + year + "-" + uniqueId + "-" + String.format("%02d", counter++);
        }

        order.setOrderNumber(orderNumber);
        order.setUserId(cart.getUserId());

        // ✅ Récupérer le nom d'utilisateur depuis localStorage via le frontend
        // ou depuis une source de données. Pour l'instant, on laisse vide ou on met une valeur par défaut
        order.setUserName(getUserNameFromCart(cart));
        order.setUserEmail(null);  // Sera mis à jour par le frontend ou plus tard
        order.setUserPhone(null);  // Sera mis à jour par le frontend ou plus tard

        // Copier les articles du panier
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setMachineId(ci.getItemId());
            oi.setMachineName(ci.getItemName());
            oi.setMachineImageUrl(ci.getItemImageUrl());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setPriceUnit(ci.getPriceUnit());
            oi.setQuantity(ci.getQuantity());
            oi.setTotalPrice(ci.getTotalPrice());
            oi.setSupplierId(ci.getSupplierProviderId());
            oi.setSupplierName(ci.getSupplierProviderName());
            order.getItems().add(oi);
        }

        // Livraison
        order.setDeliveryAddress(cart.getDeliveryAddress());
        order.setDeliveryCity(cart.getDeliveryCity());
        order.setDeliveryZipCode(cart.getDeliveryZipCode());
        order.setDeliveryPhone(cart.getDeliveryPhone());
        order.setDeliveryCost(cart.getDeliveryCost() != null ? cart.getDeliveryCost() : 0.0);
        order.setDeliveryType("STANDARD");

        // Prix
        double subtotal = cart.getItems().stream()
                .mapToDouble(i -> i.getTotalPrice() != null ? i.getTotalPrice() : 0)
                .sum();
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal + order.getDeliveryCost());
        order.setStatus(OrderStatus.PENDING);

        // Timestamps (gérés par @PrePersist)
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        Order saved = orderRepository.save(order);

        // ✅ Notifier l'utilisateur
        notificationService.create(
                cart.getUserId(),
                NotificationType.ORDER_CONFIRMED,
                "🛒 Commande créée — " + saved.getOrderNumber(),
                "Votre commande de " + order.getItems().size() + " article(s) est en attente de confirmation. Total : " + order.getTotalAmount() + " TND",
                saved.getId(), "ORDER",
                "/orders/" + saved.getId()
        );

        return saved;
    }

    // ✅ Méthode helper pour récupérer le nom d'utilisateur
    private String getUserNameFromCart(Cart cart) {
        // Si le Cart a un champ userName, l'utiliser
        // Sinon, retourner une valeur par défaut
        // Le frontend mettra à jour via updateStatus plus tard
        return "Utilisateur-" + cart.getUserId();
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElse(null);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Order> getByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> getByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public Order updateStatus(Long orderId, OrderStatus status, String adminNote) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        order.setStatus(status);
        if (adminNote != null) order.setAdminNote(adminNote);
        order.setUpdatedAt(LocalDateTime.now());

        if (status == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());

            // ✅ CRÉDITER LES POINTS DE FIDÉLITÉ
            if (order.getUserId() != null && order.getTotalAmount() != null) {
                String userName = order.getUserName() != null ? order.getUserName() : "Utilisateur";
                try {
                    loyaltyService.earnPointsForOrder(
                            order.getUserId(),
                            userName,
                            order.getId(),
                            order.getTotalAmount()
                    );
                } catch (Exception e) {
                    System.err.println("Erreur lors du crédit des points de fidélité: " + e.getMessage());
                }
            }
        }

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);

        // Notifications
        switch (status) {
            case CONFIRMED:
                notificationService.create(order.getUserId(), NotificationType.ORDER_CONFIRMED,
                        "✅ Commande confirmée — " + order.getOrderNumber(),
                        "Votre commande a été confirmée par l'administrateur.",
                        order.getId(), "ORDER", "/orders/" + order.getId());
                break;
            case IN_DELIVERY:
                notificationService.create(order.getUserId(), NotificationType.ORDER_IN_DELIVERY,
                        "🚚 Commande en livraison — " + order.getOrderNumber(),
                        "Votre commande est en route !",
                        order.getId(), "ORDER", "/orders/" + order.getId());
                break;
            case DELIVERED:
                notificationService.create(order.getUserId(), NotificationType.ORDER_DELIVERED,
                        "🎉 Commande livrée — " + order.getOrderNumber(),
                        "Votre commande a été livrée. Merci !",
                        order.getId(), "ORDER", "/orders/" + order.getId());
                break;
            case CANCELLED:
                notificationService.create(order.getUserId(), NotificationType.ORDER_CANCELLED,
                        "❌ Commande annulée — " + order.getOrderNumber(),
                        "Votre commande a été annulée." + (adminNote != null ? " Note : " + adminNote : ""),
                        order.getId(), "ORDER", "/orders/" + order.getId());
                break;
            default:
                break;
        }

        return saved;
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}