package org.example.backend_pi.service;
// service/impl/CartServiceImpl.java
// ✅ NOUVEAU : notification "stock faible" quand on ajoute au panier
//    si la machine a stockQuantity <= 3 (seuil configurable)

import org.example.backend_pi.dto.AddToCartDTO;
import org.example.backend_pi.dto.DeliveryInfoDTO;
import org.example.backend_pi.entity.*;
import org.example.backend_pi.enums.DeliveryType;
import org.example.backend_pi.enums.ItemType;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    // ─── SEUIL STOCK FAIBLE ────────────────────────────────────
    // Si le stock restant après l'ajout est <= ce seuil, on notifie
    private static final int LOW_STOCK_THRESHOLD = 3;

    @Autowired private CartRepository      cartRepository;
    @Autowired private CartItemRepository  cartItemRepository;
    @Autowired private MachineRepository   machineRepository;
    @Autowired private OrderService        orderService;

    // ✅ Injecté pour envoyer les alertes stock faible
    @Autowired private NotificationService notificationService;

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Override
    public Cart addToCart(Long userId, AddToCartDTO dto) {
        if (!dto.getItemType().equals("MACHINE"))
            throw new RuntimeException("Seules les machines peuvent être ajoutées au panier.");

        Cart cart = getCartByUserId(userId);
        Machine machine = machineRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));

        if (machine.getStockQuantity() != null && machine.getStockQuantity() <= 0)
            throw new RuntimeException("Machine non disponible en stock");

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getItemType() == ItemType.MACHINE
                        && i.getItemId().equals(dto.getItemId()))
                .findFirst().orElse(null);

        if (existing != null) {
            int newQty = existing.getQuantity() + dto.getQuantity();
            if (machine.getStockQuantity() != null && newQty > machine.getStockQuantity())
                throw new RuntimeException("Quantité demandée dépasse le stock disponible");
            existing.setQuantity(newQty);
            existing.calculateTotalPrice();
            cartItemRepository.save(existing);
        } else {
            CartItem item = createCartItem(machine, dto.getQuantity());
            item.setCart(cart);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);

        // ✅ Vérifier le stock APRÈS l'ajout et envoyer une notification si faible
        sendLowStockNotificationIfNeeded(userId, machine, dto.getQuantity());

        return saved;
    }

    /**
     * ✅ NOUVEAU : Envoie une notification "Stock faible" à l'utilisateur
     * si la quantité restante en stock est <= LOW_STOCK_THRESHOLD après l'ajout.
     *
     * Ex: stock = 2, l'utilisateur vient d'ajouter 1 unité au panier
     * → "⚠️ Dépêchez-vous ! Il ne reste que 2 exemplaires de [Nom Machine]"
     */
    private void sendLowStockNotificationIfNeeded(Long userId, Machine machine, int addedQty) {
        if (machine.getStockQuantity() == null) return;

        // Stock restant = stock actuel en DB (avant décrémentation — décrémentée seulement au checkout)
        // On calcule le "stock libre" = stock DB - total dans tous les paniers
        // Pour simplifier, on regarde juste le stock DB courant
        int stockRemaining = machine.getStockQuantity();

        if (stockRemaining <= LOW_STOCK_THRESHOLD && stockRemaining > 0) {
            String urgencyEmoji = stockRemaining == 1 ? "🚨" : "⚠️";
            String title = urgencyEmoji + " Stock quasi épuisé — " + machine.getName();
            String content;

            if (stockRemaining == 1) {
                content = "⚡ Dernier exemplaire disponible ! "
                        + "Vous avez \"" + machine.getName()
                        + "\" dans votre panier — finalisez votre commande avant qu'il soit trop tard !";
            } else {
                content = "⚡ Plus que " + stockRemaining + " exemplaire(s) disponible(s) pour \""
                        + machine.getName() + "\". "
                        + "Cette machine est dans votre panier — dépêchez-vous de passer commande !";
            }

            notificationService.create(
                    userId,
                    NotificationType.MACHINE_LOW_STOCK,
                    title,
                    content,
                    machine.getId(), "MACHINE",
                    "/cart"           // lien direct vers le panier
            );
        }
    }

    @Override
    public Cart removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        cartItemRepository.deleteById(cartItemId);
        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = getCartByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Item non trouvé"));

        if (quantity <= 0) return removeFromCart(userId, cartItemId);

        Machine machine = machineRepository.findById(item.getItemId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));
        if (machine.getStockQuantity() != null && quantity > machine.getStockQuantity())
            throw new RuntimeException("Quantité demandée dépasse le stock disponible");

        item.setQuantity(quantity);
        item.calculateTotalPrice();
        cartItemRepository.save(item);
        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    public Cart clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setDeliveryCost(0.0);
        cart.setTotalAmount(0.0);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateDeliveryInfo(Long userId, DeliveryInfoDTO dto) {
        Cart cart = getCartByUserId(userId);
        if (dto.getDeliveryAddress() != null) cart.setDeliveryAddress(dto.getDeliveryAddress());
        if (dto.getDeliveryCity()    != null) cart.setDeliveryCity(dto.getDeliveryCity());
        if (dto.getDeliveryZipCode() != null) cart.setDeliveryZipCode(dto.getDeliveryZipCode());
        if (dto.getDeliveryPhone()   != null) cart.setDeliveryPhone(dto.getDeliveryPhone());
        cart.setUpdatedAt(LocalDateTime.now());
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Override
    public Cart calculateDeliveryCost(Long userId, String deliveryType) {
        Cart cart = getCartByUserId(userId);
        try {
            cart.setDeliveryCost(DeliveryType.valueOf(deliveryType.toUpperCase()).getDefaultCost());
        } catch (IllegalArgumentException e) {
            cart.setDeliveryCost(5.0);
        }
        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    public String checkout(Long userId) {
        Cart cart = getCartByUserId(userId);

        if (cart.getItems().isEmpty())
            throw new RuntimeException("Le panier est vide");
        if (cart.getDeliveryAddress() == null || cart.getDeliveryAddress().isEmpty())
            throw new RuntimeException("L'adresse de livraison est requise");

        for (CartItem item : cart.getItems()) {
            Machine machine = machineRepository.findById(item.getItemId())
                    .orElseThrow(() -> new RuntimeException("Machine non trouvée : " + item.getItemName()));
            if (machine.getStockQuantity() != null && item.getQuantity() > machine.getStockQuantity())
                throw new RuntimeException("Stock insuffisant pour : " + machine.getName());
            if (machine.getStockQuantity() != null) {
                machine.setStockQuantity(machine.getStockQuantity() - item.getQuantity());
                machineRepository.save(machine);
            }
        }

        Order order = orderService.createFromCart(cart);
        clearCart(userId);
        return order != null ? order.getOrderNumber() : null;
    }

    // ─── PRIVÉS ──────────────────────────────────────────────────

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDeliveryCost(0.0);
        cart.setTotalAmount(0.0);
        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Machine machine, Integer quantity) {
        CartItem item = new CartItem();
        item.setItemType(ItemType.MACHINE);
        item.setItemId(machine.getId());
        item.setQuantity(quantity);
        item.setItemName(machine.getName());
        item.setItemImageUrl(machine.getImageUrls().isEmpty() ? null : machine.getImageUrls().get(0));
        item.setUnitPrice(machine.getPrice());
        item.setPriceUnit(machine.getPriceUnit());
        item.setSupplierProviderId(machine.getSupplierId());
        item.setSupplierProviderName(machine.getSupplierName());
        item.calculateTotalPrice();
        return item;
    }
}