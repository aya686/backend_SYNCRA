package org.example.backend_pi.dto;

// dto/ServiceDTO.java
import lombok.Data;
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
}