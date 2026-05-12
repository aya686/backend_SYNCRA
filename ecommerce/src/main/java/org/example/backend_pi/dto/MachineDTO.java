package org.example.backend_pi.dto;

// dto/MachineDTO.java

import lombok.Data;

import java.time.LocalDateTime;
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
    private String validationStatus;
    private String subCategory;
    private String businessType;         // Type d'entreprise : MANUFACTURER, DISTRIBUTOR...
    private String rejectionReason;      // Motif du rejet
    private LocalDateTime rejectedAt;    // Date de rejet
    private LocalDateTime approvedAt;    // Date d'approbation
    private Long validatedBy;

}