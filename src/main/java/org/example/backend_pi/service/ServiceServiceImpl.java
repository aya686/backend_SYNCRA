package org.example.backend_pi.service;
// service/impl/ServiceServiceImpl.java
// ✅ MODÉRATION AUTO : appel à ContentModerationService dans addService()

import org.example.backend_pi.dto.ServiceDTO;
import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.enums.*;
import org.example.backend_pi.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    // ✅ INJECTION DU SERVICE DE MODÉRATION
    @Autowired
    private ContentModerationService moderationService;

    @Override
    public ServiceEntity addService(ServiceDTO serviceDTO) {

        // ── ✅ MODÉRATION AUTOMATIQUE ──────────────────────────────
        ContentModerationService.ModerationResult modResult =
                moderationService.moderate(
                        serviceDTO.getName(),
                        serviceDTO.getDescription(),
                        "SERVICE",
                        null
                );

        ServiceEntity service = new ServiceEntity();
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());

        if (serviceDTO.getServiceType() != null)
            service.setServiceType(ServiceType.valueOf(serviceDTO.getServiceType()));
        if (serviceDTO.getCategory() != null)
            service.setCategory(CategoryType.valueOf(serviceDTO.getCategory()));
        if (serviceDTO.getAvailability() != null)
            service.setAvailability(AvailabilityStatus.valueOf(serviceDTO.getAvailability()));
        else
            service.setAvailability(AvailabilityStatus.AVAILABLE);

        service.setBasePrice(serviceDTO.getBasePrice());
        service.setPriceUnit(serviceDTO.getPriceUnit());
        service.setLocation(serviceDTO.getLocation());
        service.setContactInfo(serviceDTO.getContactInfo());
        service.setImageUrls(serviceDTO.getImageUrls());
        service.setProviderId(serviceDTO.getProviderId());
        service.setProviderName(serviceDTO.getProviderName());
        service.setProviderCompanyName(serviceDTO.getProviderCompanyName());
        service.setIsInApp(serviceDTO.getIsInApp() != null ? serviceDTO.getIsInApp() : true);

        // ✅ SOUS-CATÉGORIE
        service.setSubCategory(serviceDTO.getSubCategory());

        // ✅ TYPE D'ENTREPRISE
        if (serviceDTO.getBusinessType() != null && !serviceDTO.getBusinessType().isBlank()) {
            try {
                service.setBusinessType(org.example.backend_pi.enums.BusinessType.valueOf(serviceDTO.getBusinessType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (!modResult.approved) {
            service.setValidationStatus("AUTO_REJECTED");
            service.setRejectionReason(modResult.reason);
            service.setRejectedAt(LocalDateTime.now());
        } else {
            service.setValidationStatus("PENDING");
        }

        return serviceRepository.save(service);
    }

    @Override
    public ServiceEntity updateService(Long id, ServiceDTO serviceDTO) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé: " + id));

        if (serviceDTO.getName() != null)             service.setName(serviceDTO.getName());
        if (serviceDTO.getDescription() != null)      service.setDescription(serviceDTO.getDescription());
        if (serviceDTO.getBasePrice() != null)        service.setBasePrice(serviceDTO.getBasePrice());
        if (serviceDTO.getPriceUnit() != null)        service.setPriceUnit(serviceDTO.getPriceUnit());
        if (serviceDTO.getLocation() != null)         service.setLocation(serviceDTO.getLocation());
        if (serviceDTO.getContactInfo() != null)      service.setContactInfo(serviceDTO.getContactInfo());
        if (serviceDTO.getImageUrls() != null)        service.setImageUrls(serviceDTO.getImageUrls());
        if (serviceDTO.getCategory() != null)         service.setCategory(CategoryType.valueOf(serviceDTO.getCategory()));
        if (serviceDTO.getAvailability() != null)     service.setAvailability(AvailabilityStatus.valueOf(serviceDTO.getAvailability()));
        if (serviceDTO.getServiceType() != null)      service.setServiceType(ServiceType.valueOf(serviceDTO.getServiceType()));
        if (serviceDTO.getProviderId() != null)       service.setProviderId(serviceDTO.getProviderId());
        if (serviceDTO.getProviderName() != null)     service.setProviderName(serviceDTO.getProviderName());
        if (serviceDTO.getProviderCompanyName() != null) service.setProviderCompanyName(serviceDTO.getProviderCompanyName());
        if (serviceDTO.getIsInApp() != null)          service.setIsInApp(serviceDTO.getIsInApp());

        // ✅ SOUS-CATÉGORIE
        if (serviceDTO.getSubCategory() != null)      service.setSubCategory(serviceDTO.getSubCategory());

        // ✅ TYPE D'ENTREPRISE
        if (serviceDTO.getBusinessType() != null && !serviceDTO.getBusinessType().isBlank()) {
            try {
                service.setBusinessType(org.example.backend_pi.enums.BusinessType.valueOf(serviceDTO.getBusinessType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (serviceDTO.getValidationStatus() != null) service.setValidationStatus(serviceDTO.getValidationStatus());
        if (serviceDTO.getRejectionReason() != null)  service.setRejectionReason(serviceDTO.getRejectionReason());
        if (serviceDTO.getApprovedAt() != null)       service.setApprovedAt(serviceDTO.getApprovedAt());
        if (serviceDTO.getRejectedAt() != null)       service.setRejectedAt(serviceDTO.getRejectedAt());
        if (serviceDTO.getValidatedBy() != null)      service.setValidatedBy(serviceDTO.getValidatedBy());

        // ✅ Re-modérer si contenu AUTO_REJECTED est mis à jour
        if ("AUTO_REJECTED".equals(service.getValidationStatus())
                && serviceDTO.getName() != null && serviceDTO.getDescription() != null) {
            ContentModerationService.ModerationResult recheck =
                    moderationService.moderate(service.getName(), service.getDescription(), "SERVICE", id);
            if (recheck.approved) {
                service.setValidationStatus("PENDING");
                service.setRejectionReason(null);
                service.setRejectedAt(null);
            } else {
                service.setValidationStatus("AUTO_REJECTED");
                service.setRejectionReason(recheck.reason);
                service.setRejectedAt(LocalDateTime.now());
            }
        }

        service.setUpdatedAt(LocalDateTime.now());
        return serviceRepository.save(service);
    }

    @Override
    public void deleteService(Long id) {
        serviceRepository.delete(
                serviceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Service non trouvé: " + id))
        );
    }

    @Override public List<ServiceEntity> getAllServices()                       { return serviceRepository.findAll(); }
    @Override public ServiceEntity getServiceById(Long id)                      { return serviceRepository.findById(id).orElse(null); }
    @Override public List<ServiceEntity> getServicesByProvider(Long pid)        { return serviceRepository.findByProviderId(pid); }
    @Override public List<ServiceEntity> getServicesByCategory(CategoryType c)  { return serviceRepository.findByCategory(c); }
    @Override public List<ServiceEntity> getAvailableServices()                 { return serviceRepository.findByAvailability(AvailabilityStatus.AVAILABLE); }

    @Override
    public List<ServiceEntity> searchWithFilters(String keyword, CategoryType category, String location, Double minPrice, Double maxPrice) {
        return serviceRepository.searchWithFilters(keyword, category, location, minPrice, maxPrice);
    }

    @Override
    public List<ServiceEntity> getServicesByServiceType(String t) {
        return serviceRepository.findByServiceType(ServiceType.valueOf(t.toUpperCase()));
    }

    @Override
    public ServiceEntity approveService(Long id) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé: " + id));
        s.setValidationStatus("APPROVED");
        s.setApprovedAt(LocalDateTime.now());
        s.setRejectionReason(null);
        s.setUpdatedAt(LocalDateTime.now());
        return serviceRepository.save(s);
    }

    @Override
    public ServiceEntity rejectService(Long id, String reason) {
        ServiceEntity s = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé: " + id));
        s.setValidationStatus("REJECTED");
        s.setRejectionReason(reason);
        s.setRejectedAt(LocalDateTime.now());
        s.setApprovedAt(null);
        s.setUpdatedAt(LocalDateTime.now());
        return serviceRepository.save(s);
    }

    @Override public List<ServiceEntity> getPendingServices()  { return serviceRepository.findByValidationStatus("PENDING"); }
    @Override public List<ServiceEntity> getApprovedServices() { return serviceRepository.findByValidationStatus("APPROVED"); }
    @Override public List<ServiceEntity> getRejectedServices() { return serviceRepository.findByValidationStatus("REJECTED"); }

    public List<ServiceEntity> getAutoRejectedServices() {
        return serviceRepository.findByValidationStatus("AUTO_REJECTED");
    }
}