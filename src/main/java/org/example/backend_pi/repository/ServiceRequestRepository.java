package org.example.backend_pi.repository;

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

    List<ServiceRequest> findByStatus(RequestStatus status);

    List<ServiceRequest> findByRequestType(RequestType requestType);

    List<ServiceRequest> findByIsExternalProvider(Boolean isExternalProvider);
}