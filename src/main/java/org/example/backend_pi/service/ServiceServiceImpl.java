package org.example.backend_pi.service;
// service/impl/ServiceServiceImpl.java


import org.example.backend_pi.dto.ServiceDTO;
import org.example.backend_pi.entity.ServiceEntity;  // Import de votre entité
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.ServiceType;
import org.example.backend_pi.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service  // Cette annotation Spring est correcte
@Transactional
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;



    @Override
    public ServiceEntity addService(ServiceDTO serviceDTO) {
        ServiceEntity service = new ServiceEntity();  // Utiliser ServiceEntity
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());

        // Set enum values with null check
        if (serviceDTO.getServiceType() != null) {
            service.setServiceType(ServiceType.valueOf(serviceDTO.getServiceType()));
        }
        if (serviceDTO.getCategory() != null) {
            service.setCategory(CategoryType.valueOf(serviceDTO.getCategory()));
        }


        if (serviceDTO.getAvailability() != null) {
            service.setAvailability(AvailabilityStatus.valueOf(serviceDTO.getAvailability()));
        } else {
            service.setAvailability(AvailabilityStatus.AVAILABLE);
        }

        service.setBasePrice(serviceDTO.getBasePrice());
        service.setPriceUnit(serviceDTO.getPriceUnit());
        service.setLocation(serviceDTO.getLocation());
        service.setContactInfo(serviceDTO.getContactInfo());
        service.setImageUrls(serviceDTO.getImageUrls());
        service.setProviderId(serviceDTO.getProviderId());
        service.setProviderName(serviceDTO.getProviderName());
        service.setProviderCompanyName(serviceDTO.getProviderCompanyName());

        service.setIsInApp(serviceDTO.getIsInApp() != null ? serviceDTO.getIsInApp() : true);


        return serviceRepository.save(service);
    }

    @Override
    public ServiceEntity updateService(Long id, ServiceDTO serviceDTO) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + id));

        // Update all fields (même si null, on met à jour avec les nouvelles valeurs)
        if (serviceDTO.getName() != null) {
            service.setName(serviceDTO.getName());
        }
        if (serviceDTO.getDescription() != null) {
            service.setDescription(serviceDTO.getDescription());
        }
        if (serviceDTO.getBasePrice() != null) {
            service.setBasePrice(serviceDTO.getBasePrice());
        }
        if (serviceDTO.getPriceUnit() != null) {
            service.setPriceUnit(serviceDTO.getPriceUnit());
        }
        if (serviceDTO.getLocation() != null) {
            service.setLocation(serviceDTO.getLocation());
        }
        if (serviceDTO.getContactInfo() != null) {
            service.setContactInfo(serviceDTO.getContactInfo());
        }
        if (serviceDTO.getImageUrls() != null) {
            service.setImageUrls(serviceDTO.getImageUrls());
        }
        if (serviceDTO.getCategory() != null) {
            service.setCategory(CategoryType.valueOf(serviceDTO.getCategory()));
        }
        if (serviceDTO.getAvailability() != null) {
            service.setAvailability(AvailabilityStatus.valueOf(serviceDTO.getAvailability()));
        }
        if (serviceDTO.getServiceType() != null) {
            service.setServiceType(ServiceType.valueOf(serviceDTO.getServiceType()));
        }
        if (serviceDTO.getProviderId() != null) {
            service.setProviderId(serviceDTO.getProviderId());
        }
        if (serviceDTO.getProviderName() != null) {
            service.setProviderName(serviceDTO.getProviderName());
        }
        if (serviceDTO.getProviderCompanyName() != null) {
            service.setProviderCompanyName(serviceDTO.getProviderCompanyName());
        }
        if (serviceDTO.getIsInApp() != null) {
            service.setIsInApp(serviceDTO.getIsInApp());
        }

        service.setUpdatedAt(LocalDateTime.now());

        return serviceRepository.save(service);
    }


    @Override
    public void deleteService(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + id));
        serviceRepository.delete(service);
    }

    @Override
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }
    @Override
    public ServiceEntity getServiceById(Long id) {
        return serviceRepository.findById(id).orElse(null);
    }

    @Override
    public List<ServiceEntity> getServicesByProvider(Long providerId) {
        return serviceRepository.findByProviderId(providerId);
    }
    @Override
    public List<ServiceEntity> getServicesByCategory(CategoryType category) {
        return serviceRepository.findByCategory(category);
    }

    @Override
    public List<ServiceEntity> searchWithFilters(String keyword, CategoryType category, String location, Double minPrice, Double maxPrice) {
        return serviceRepository.searchWithFilters(keyword, category, location, minPrice, maxPrice);
    }

    @Override
    public List<ServiceEntity> getAvailableServices() {
        return serviceRepository.findByAvailability(AvailabilityStatus.AVAILABLE);
    }

    @Override
    public List<ServiceEntity> getServicesByServiceType(String serviceType) {
        ServiceType type = ServiceType.valueOf(serviceType.toUpperCase());
        return serviceRepository.findByServiceType(type);
    }
}