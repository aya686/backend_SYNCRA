package org.example.backend_pi.dto;
// dto/TopItemDTO.java

import lombok.Data;

@Data
public class TopItemDTO {
    private Long id;
    private String resourceType;  // MACHINE | SERVICE
    private String name;
    private String category;
    private String imageUrl;
    private Double price;
    private String priceUnit;
    private Double rating;
    private Integer reviewCount;
    private String location;
    private String supplierName;
    private String availability;

    // Statistiques de vente
    private Long   totalSold;      // nombre total d'unités vendues
    private Double totalRevenue;   // chiffre d'affaires généré

    // Rang
    private Integer rank;
    private String  rankLabel;    // "🥇 N°1", "🥈 N°2", "Top 5"...
}