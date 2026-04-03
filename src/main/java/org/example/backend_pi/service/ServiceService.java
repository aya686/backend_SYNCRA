package org.example.backend_pi.service;

// service/ServiceService.java

import org.example.backend_pi.dto.ServiceDTO;
import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.enums.CategoryType;

import java.util.List;

public interface ServiceService {

    ServiceEntity addService(ServiceDTO serviceDTO);

    ServiceEntity updateService(Long id, ServiceDTO serviceDTO);

    void deleteService(Long id);

    List<ServiceEntity> getAllServices();

    ServiceEntity getServiceById(Long id);

    List<ServiceEntity> getServicesByProvider(Long providerId);
    List<ServiceEntity> getServicesByCategory(CategoryType category);

    List<ServiceEntity> searchWithFilters(String keyword, CategoryType category, String location, Double minPrice, Double maxPrice);

    List<ServiceEntity> getAvailableServices();

    List<ServiceEntity> getServicesByServiceType(String serviceType);
}