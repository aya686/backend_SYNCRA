package org.example.backend_pi.dto;
// dto/ServiceDTO.java

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String serviceType;
    private Double basePrice;
    private String priceUnit;
    private String location;
    private String contactInfo;
    private String availability;
    private List<String> imageUrls;
    private Long providerId;
    private String providerName;
    private String providerCompanyName;
    private Boolean isInApp;

    private String validationStatus;
    private String subCategory;
    private String businessType;         // Type d'entreprise : MANUFACTURER, DISTRIBUTOR...
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private LocalDateTime approvedAt;
    private Long validatedBy;
}