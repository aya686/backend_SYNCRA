package org.example.backend_pi.entity;

// entity/ServiceEntity.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.BusinessType;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.ServiceType;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    private Double basePrice;
    private String priceUnit;
    private String location;
    private String contactInfo;
    private Double rating;
    private Integer reviewCount;

    @ElementCollection
    @CollectionTable(name = "service_images", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    private Long providerId;
    private String providerName;
    private String providerCompanyName;
    private Boolean isInApp;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 50)
    private BusinessType businessType;

    // ✅ AJOUTER CES CHAMPS POUR LA VALIDATION
    @Column(name = "validation_status", nullable = false)
    private String validationStatus = "PENDING";

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "validated_by")
    private Long validatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (validationStatus == null) {
            validationStatus = "PENDING";
        }
        if (rating == null) rating = 0.0;
        if (reviewCount == null) reviewCount = 0;
        if (isInApp == null) isInApp = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}