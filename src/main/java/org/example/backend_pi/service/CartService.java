package org.example.backend_pi.service;
// service/CartService.java


import org.example.backend_pi.entity.Cart;
import org.example.backend_pi.dto.AddToCartDTO;
import org.example.backend_pi.dto.DeliveryInfoDTO;

public interface CartService {

    Cart getCartByUserId(Long userId);

    Cart addToCart(Long userId, AddToCartDTO addToCartDTO);

    Cart removeFromCart(Long userId, Long cartItemId);

    Cart updateQuantity(Long userId, Long cartItemId, Integer quantity);

    Cart clearCart(Long userId);

    Cart updateDeliveryInfo(Long userId, DeliveryInfoDTO deliveryInfoDTO);

    Cart calculateDeliveryCost(Long userId, String deliveryType);

    void checkout(Long userId);
}