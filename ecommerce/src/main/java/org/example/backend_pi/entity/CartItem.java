package org.example.backend_pi.entity;

// entity/CartItem.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.backend_pi.enums.ItemType;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "item_image_url")
    private String itemImageUrl;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "price_unit", length = 50)
    private String priceUnit;

    @Column(name = "supplier_provider_id")
    private Long supplierProviderId;

    @Column(name = "supplier_provider_name", length = 100)
    private String supplierProviderName;

    private Integer quantity = 1;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        if (quantity == null) quantity = 1;
        calculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = this.unitPrice * this.quantity;
        } else {
            this.totalPrice = 0.0;
        }
    }
}