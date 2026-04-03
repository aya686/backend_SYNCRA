package org.example.backend_pi.entity;
// entity/Service.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.ServiceType;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "base_price")
    private Double basePrice;

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

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "matching_score")
    private Double matchingScore;

    @ElementCollection
    @CollectionTable(name = "service_images", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "provider_company_name", length = 150)
    private String providerCompanyName;

    @Column(name = "provider_avatar_url")
    private String providerAvatarUrl;

    @Column(name = "is_in_app")
    private Boolean isInApp = true;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
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
