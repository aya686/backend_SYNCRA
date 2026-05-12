package org.example.backend_pi.service;

// service/impl/ServiceRequestServiceImpl.java

import org.example.backend_pi.dto.RequestAttachmentDTO;
import org.example.backend_pi.dto.ServiceRequestDTO;
import org.example.backend_pi.entity.RequestAttachment;
import org.example.backend_pi.entity.ServiceRequest;
import org.example.backend_pi.enums.RequestStatus;
import org.example.backend_pi.enums.RequestType;
import org.example.backend_pi.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Override
    public ServiceRequest addRequest(ServiceRequestDTO requestDTO) {
        ServiceRequest request = new ServiceRequest();

        // Basic information
        request.setTitle(requestDTO.getTitle());
        request.setDescription(requestDTO.getDescription());

        // Requester information
        request.setRequesterId(requestDTO.getRequesterId());
        request.setRequesterName(requestDTO.getRequesterName());
        request.setRequesterEmail(requestDTO.getRequesterEmail());
        request.setRequesterPhone(requestDTO.getRequesterPhone());

        // Provider information
        request.setTargetProviderId(requestDTO.getTargetProviderId());
        request.setIsExternalProvider(requestDTO.getIsExternalProvider() != null ?
                requestDTO.getIsExternalProvider() : false);

        if (Boolean.TRUE.equals(request.getIsExternalProvider())) {
            request.setExternalProviderName(requestDTO.getExternalProviderName());
            request.setExternalProviderPhone(requestDTO.getExternalProviderPhone());
            request.setExternalProviderEmail(requestDTO.getExternalProviderEmail());
            request.setExternalProviderCompany(requestDTO.getExternalProviderCompany());
        }

        // Request details
        if (requestDTO.getRequestType() != null) {
            request.setRequestType(RequestType.valueOf(requestDTO.getRequestType()));
        }

        request.setMachineServiceId(requestDTO.getMachineServiceId());
        request.setMachineServiceName(requestDTO.getMachineServiceName());
        request.setQuantity(requestDTO.getQuantity());
        request.setMaterial(requestDTO.getMaterial());
        request.setDeadline(requestDTO.getDeadline());
        request.setSpecifications(requestDTO.getSpecifications());
        request.setBudget(requestDTO.getBudget());

        // Status
        request.setStatus(RequestStatus.PENDING);

        // AJOUTER LES ATTACHMENTS
        if (requestDTO.getAttachments() != null && !requestDTO.getAttachments().isEmpty()) {
            List<RequestAttachment> attachments = new ArrayList<>();
            for (RequestAttachmentDTO attDTO : requestDTO.getAttachments()) {
                RequestAttachment attachment = new RequestAttachment();
                attachment.setFileName(attDTO.getFileName());
                attachment.setFileUrl(attDTO.getFileUrl());
                attachment.setFileType(attDTO.getFileType());
                attachment.setFileSize(attDTO.getFileSize());
                attachments.add(attachment);
            }
            request.setAttachments(attachments);
        }

        return requestRepository.save(request);
    }

    @Override
    public ServiceRequest updateRequest(Long id, ServiceRequestDTO requestDTO) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + id));

        // Mettre à jour les champs simples
        if (requestDTO.getTitle() != null) request.setTitle(requestDTO.getTitle());
        if (requestDTO.getDescription() != null) request.setDescription(requestDTO.getDescription());
        if (requestDTO.getQuantity() != null) request.setQuantity(requestDTO.getQuantity());
        if (requestDTO.getMaterial() != null) request.setMaterial(requestDTO.getMaterial());
        if (requestDTO.getDeadline() != null) request.setDeadline(requestDTO.getDeadline());
        if (requestDTO.getSpecifications() != null) request.setSpecifications(requestDTO.getSpecifications());
        if (requestDTO.getBudget() != null) request.setBudget(requestDTO.getBudget());

        // Mettre à jour les attachments - EFFACER LES ANCIENS ET AJOUTER LES NOUVEAUX
        if (requestDTO.getAttachments() != null) {
            // Effacer tous les anciens attachments
            request.getAttachments().clear();

            // Ajouter les nouveaux attachments
            List<RequestAttachment> newAttachments = new ArrayList<>();
            for (RequestAttachmentDTO attDTO : requestDTO.getAttachments()) {
                RequestAttachment attachment = new RequestAttachment();
                attachment.setFileName(attDTO.getFileName());
                attachment.setFileUrl(attDTO.getFileUrl());
                attachment.setFileType(attDTO.getFileType());
                attachment.setFileSize(attDTO.getFileSize());
                newAttachments.add(attachment);
            }
            request.setAttachments(newAttachments);
        }

        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    @Override
    public void deleteRequest(Long id) {
        if (!requestRepository.existsById(id)) {
            throw new RuntimeException("Demande non trouvée avec l'ID: " + id);
        }
        requestRepository.deleteById(id);
    }

    @Override
    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    @Override
    public ServiceRequest getRequestById(Long id) {
        return requestRepository.findById(id).orElse(null);
    }

    @Override
    public List<ServiceRequest> getRequestsByRequester(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId);
    }

    @Override
    public List<ServiceRequest> getRequestsByProvider(Long providerId) {
        return requestRepository.findByTargetProviderId(providerId);
    }

    @Override
    public List<ServiceRequest> getRequestsByStatus(String status) {
        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
        return requestRepository.findByStatus(requestStatus);
    }

    @Override
    public List<ServiceRequest> getRequestsByType(String requestType) {
        RequestType type = RequestType.valueOf(requestType.toUpperCase());
        return requestRepository.findByRequestType(type);
    }

    @Override
    public ServiceRequest updateStatus(Long id, String status, String responseMessage) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + id));

        RequestStatus newStatus = RequestStatus.valueOf(status.toUpperCase());
        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());

        if (responseMessage != null) {
            request.setResponseMessage(responseMessage);
        }

        if (newStatus == RequestStatus.ACCEPTED || newStatus == RequestStatus.REJECTED) {
            request.setRespondedAt(LocalDateTime.now());
        }

        return requestRepository.save(request);
    }

    @Override
    public ServiceRequest respondToRequest(Long id, String responseMessage, Double proposedPrice, String estimatedDelivery) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + id));

        request.setResponseMessage(responseMessage);
        if (proposedPrice != null) {
            request.setProposedPrice(proposedPrice);
        }
        if (estimatedDelivery != null) {
            request.setEstimatedDelivery(estimatedDelivery);
        }
        request.setStatus(RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    @Override
    public ServiceRequest cancelRequest(Long id) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID: " + id));

        request.setStatus(RequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

}
