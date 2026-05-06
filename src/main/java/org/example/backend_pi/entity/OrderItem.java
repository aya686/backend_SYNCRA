package org.example.backend_pi.entity;

// entity/OrderItem.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "machine_name", length = 200)
    private String machineName;

    @Column(name = "machine_image_url")
    private String machineImageUrl;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "price_unit", length = 50)
    private String priceUnit;

    private Integer quantity;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;
}