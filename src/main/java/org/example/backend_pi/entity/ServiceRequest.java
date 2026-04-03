package org.example.backend_pi.entity;

// entity/ServiceRequest.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.backend_pi.enums.RequestStatus;
import org.example.backend_pi.enums.RequestType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Requester information
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "requester_email", length = 100)
    private String requesterEmail;

    @Column(name = "requester_phone", length = 20)
    private String requesterPhone;

    // Provider information
    @Column(name = "target_provider_id")
    private Long targetProviderId;

    @Column(name = "is_external_provider")
    private Boolean isExternalProvider = false;

    @Column(name = "external_provider_name", length = 150)
    private String externalProviderName;

    @Column(name = "external_provider_phone", length = 20)
    private String externalProviderPhone;

    @Column(name = "external_provider_email", length = 100)
    private String externalProviderEmail;

    @Column(name = "external_provider_company", length = 150)
    private String externalProviderCompany;

    // Request details
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    @Column(name = "machine_service_id")
    private Long machineServiceId;

    @Column(name = "machine_service_name", length = 200)
    private String machineServiceName;

    private Integer quantity;

    @Column(length = 100)
    private String material;

    @Column(length = 50)
    private String deadline;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    private Double budget;

    // Status
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    // Attachments - AJOUTER CETTE PARTIE
    @ElementCollection
    @CollectionTable(name = "request_attachments", joinColumns = @JoinColumn(name = "request_id"))
    private List<RequestAttachment> attachments = new ArrayList<>();

    // Response
    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "proposed_price")
    private Double proposedPrice;

    @Column(name = "estimated_delivery", length = 50)
    private String estimatedDelivery;

    // Dates
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
        if (isExternalProvider == null) {
            isExternalProvider = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}