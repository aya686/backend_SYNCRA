package org.example.backend_pi.dto;
import lombok.Data;

@Data
public class DeliveryInfoDTO {
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryZipCode;
    private String deliveryPhone;
    private String deliveryType;  // STANDARD, EXPRESS, SAME_DAY
}