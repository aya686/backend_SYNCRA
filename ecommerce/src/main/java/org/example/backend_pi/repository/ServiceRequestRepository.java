package org.example.backend_pi.repository;
// repository/ServiceRequestRepository.java
// ✅ Ajouter findByStatus() pour LoyaltyService (top services)

import org.example.backend_pi.entity.ServiceRequest;
import org.example.backend_pi.enums.RequestStatus;
import org.example.backend_pi.enums.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByRequesterId(Long requesterId);
    List<ServiceRequest> findByTargetProviderId(Long providerId);
    List<ServiceRequest> findByStatus(RequestStatus status);    // ✅ pour top services
    List<ServiceRequest> findByRequestType(RequestType type);
}