package org.example.backend_pi.dto;

// dto/MachineDTO.java

import lombok.Data;
import java.util.List;

@Data
public class MachineDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String type;
    private String availability;
    private String transactionType;
    private Double price;
    private String priceUnit;
    private String location;
    private String contactInfo;
    private Integer stockQuantity;
    private List<String> imageUrls;
    private Long supplierId;
    private String supplierName;
    private String supplierCompanyName;
    private Boolean isInApp;
}