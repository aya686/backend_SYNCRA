package org.example.backend_pi.service;

// service/impl/CartServiceImpl.java (version modifiée)
// service/impl/CartServiceImpl.java (version modifiée)


import org.example.backend_pi.entity.*;
import org.example.backend_pi.enums.ItemType;
import org.example.backend_pi.enums.DeliveryType;
import org.example.backend_pi.repository.*;
import org.example.backend_pi.service.CartService;
import org.example.backend_pi.dto.AddToCartDTO;
import org.example.backend_pi.dto.DeliveryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Override
    public Cart addToCart(Long userId, AddToCartDTO addToCartDTO) {
        // Vérifier que c'est bien une machine
        if (!addToCartDTO.getItemType().equals("MACHINE")) {
            throw new RuntimeException("Seules les machines peuvent être ajoutées au panier. Pour les services, veuillez créer une demande.");
        }

        Cart cart = getCartByUserId(userId);

        // Vérifier si la machine existe
        Machine machine = machineRepository.findById(addToCartDTO.getItemId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));

        // Vérifier si la machine est disponible
        if (machine.getStockQuantity() != null && machine.getStockQuantity() <= 0) {
            throw new RuntimeException("Machine non disponible en stock");
        }

        // Vérifier si l'item existe déjà dans le panier
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getItemType() == ItemType.MACHINE
                        && item.getItemId().equals(addToCartDTO.getItemId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Augmenter la quantité
            int newQuantity = existingItem.getQuantity() + addToCartDTO.getQuantity();
            if (machine.getStockQuantity() != null && newQuantity > machine.getStockQuantity()) {
                throw new RuntimeException("Quantité demandée dépasse le stock disponible");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.calculateTotalPrice();
            cartItemRepository.save(existingItem);
        } else {
            // Ajouter nouvelle machine
            CartItem newItem = createCartItem(machine, addToCartDTO.getQuantity());
            newItem.setCart(cart);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    @Override
    public Cart removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.deleteById(cartItemId);

        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = getCartByUserId(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item non trouvé"));

        if (quantity <= 0) {
            return removeFromCart(userId, cartItemId);
        }

        // Vérifier le stock
        Machine machine = machineRepository.findById(item.getItemId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvée"));
        if (machine.getStockQuantity() != null && quantity > machine.getStockQuantity()) {
            throw new RuntimeException("Quantité demandée dépasse le stock disponible");
        }

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
    public Cart updateDeliveryInfo(Long userId, DeliveryInfoDTO deliveryInfoDTO) {
        Cart cart = getCartByUserId(userId);

        if (deliveryInfoDTO.getDeliveryAddress() != null) {
            cart.setDeliveryAddress(deliveryInfoDTO.getDeliveryAddress());
        }
        if (deliveryInfoDTO.getDeliveryCity() != null) {
            cart.setDeliveryCity(deliveryInfoDTO.getDeliveryCity());
        }
        if (deliveryInfoDTO.getDeliveryZipCode() != null) {
            cart.setDeliveryZipCode(deliveryInfoDTO.getDeliveryZipCode());
        }
        if (deliveryInfoDTO.getDeliveryPhone() != null) {
            cart.setDeliveryPhone(deliveryInfoDTO.getDeliveryPhone());
        }
        if (deliveryInfoDTO.getDeliveryType() != null) {
            calculateDeliveryCost(userId, deliveryInfoDTO.getDeliveryType());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cart.calculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    public Cart calculateDeliveryCost(Long userId, String deliveryType) {
        Cart cart = getCartByUserId(userId);

        try {
            DeliveryType type = DeliveryType.valueOf(deliveryType.toUpperCase());
            cart.setDeliveryCost(type.getDefaultCost());
        } catch (IllegalArgumentException e) {
            cart.setDeliveryCost(5.0);
        }

        cart.calculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    @Override
    public void checkout(Long userId) {
        Cart cart = getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }

        if (cart.getDeliveryAddress() == null || cart.getDeliveryAddress().isEmpty()) {
            throw new RuntimeException("L'adresse de livraison est requise");
        }

        // Vérifier le stock pour chaque machine
        for (CartItem item : cart.getItems()) {
            Machine machine = machineRepository.findById(item.getItemId())
                    .orElseThrow(() -> new RuntimeException("Machine non trouvée: " + item.getItemName()));

            if (machine.getStockQuantity() != null && item.getQuantity() > machine.getStockQuantity()) {
                throw new RuntimeException("Stock insuffisant pour: " + machine.getName());
            }

            // Réduire le stock
            if (machine.getStockQuantity() != null) {
                machine.setStockQuantity(machine.getStockQuantity() - item.getQuantity());
                machineRepository.save(machine);
            }
        }

        // Ici, vous pouvez créer une commande (Order)
        // Pour l'instant, on vide juste le panier
        clearCart(userId);
    }

    // Méthodes privées
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