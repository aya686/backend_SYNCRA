package org.example.backend_pi.entity;

// entity/Machine.java
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.backend_pi.enums.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "machines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 50)
    private BusinessType businessType;


    @Enumerated(EnumType.STRING)
    @Column(name = "machine_type")
    private MachineType type;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    private Double price;

    @Column(name = "price_unit", length = 50)
    private String priceUnit;

    @Column(length = 255)
    private String location;

    @Column(name = "location_latitude")
    private String locationLatitude;

    @Column(name = "location_longitude")
    private String locationLongitude;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "matching_score")
    private Double matchingScore;

    @ElementCollection
    @CollectionTable(name = "machine_images", joinColumns = @JoinColumn(name = "machine_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @Column(name = "supplier_company_name", length = 150)
    private String supplierCompanyName;

    @Column(name = "supplier_avatar_url")
    private String supplierAvatarUrl;

    @Column(name = "is_in_app")
    private Boolean isInApp = true;

    @Column(name = "technical_specs", columnDefinition = "TEXT")
    private String technicalSpecs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private CategoryType category;

    @Column(name = "sub_category", length = 100)
    private String subCategory; // Sous-catégorie spécifique (ex: tractors, irrigation...)

    @Column(name = "validation_status")
    private String validationStatus; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "validated_by")
    private Long validatedBy; // ID de l'admin qui a validé/rejeté
    @JsonIgnore
    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (rating == null) rating = 0.0;
        if (reviewCount == null) reviewCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}