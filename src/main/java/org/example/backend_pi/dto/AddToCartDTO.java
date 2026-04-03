package org.example.backend_pi.dto;

import lombok.Data;

@Data
public class AddToCartDTO {
    private String itemType;  // Doit être "MACHINE"
    private Long itemId;
    private Integer quantity = 1;
}