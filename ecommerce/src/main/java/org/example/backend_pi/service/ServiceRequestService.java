package org.example.backend_pi.service;

// service/ServiceRequestService.java

import org.example.backend_pi.dto.ServiceRequestDTO;
import org.example.backend_pi.entity.ServiceRequest;

import java.util.List;

public interface ServiceRequestService {

    ServiceRequest addRequest(ServiceRequestDTO requestDTO);

    ServiceRequest updateRequest(Long id, ServiceRequestDTO requestDTO);

    void deleteRequest(Long id);

    List<ServiceRequest> getAllRequests();

    ServiceRequest getRequestById(Long id);

    List<ServiceRequest> getRequestsByRequester(Long requesterId);

    List<ServiceRequest> getRequestsByProvider(Long providerId);

    List<ServiceRequest> getRequestsByStatus(String status);

    List<ServiceRequest> getRequestsByType(String requestType);

    ServiceRequest updateStatus(Long id, String status, String responseMessage);

    ServiceRequest respondToRequest(Long id, String responseMessage, Double proposedPrice, String estimatedDelivery);

    ServiceRequest cancelRequest(Long id);
}