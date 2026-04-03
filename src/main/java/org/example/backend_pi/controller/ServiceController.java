package org.example.backend_pi.controller;

// controller/ServiceController.java


import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.service.ServiceService;
import org.example.backend_pi.dto.ServiceDTO;
import org.example.backend_pi.enums.CategoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // Ajouter un service
    @PostMapping
    public ResponseEntity<ServiceEntity> addService(@RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceEntity service = serviceService.addService(serviceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Modifier un service
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable Long id, @RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceEntity service = serviceService.updateService(id, serviceDTO);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // Supprimer un service (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.ok("Service supprimé définitivement avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        }
    }

    // Récupérer tous les services
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        List<ServiceEntity> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    // Récupérer un service par ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        ServiceEntity service = serviceService.getServiceById(id);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service);
    }

    // Récupérer les services d'un prestataire
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceEntity>> getServicesByProvider(@PathVariable Long providerId) {
        List<ServiceEntity> services = serviceService.getServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    // Récupérer les services par catégorie
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ServiceEntity>> getServicesByCategory(@PathVariable String category) {
        try {
            CategoryType categoryType = CategoryType.valueOf(category.toUpperCase());
            List<ServiceEntity> services = serviceService.getServicesByCategory(categoryType);
            return ResponseEntity.ok(services);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Rechercher avec filtres
    @GetMapping("/search")
    public ResponseEntity<List<ServiceEntity>> searchServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        try {
            CategoryType categoryType = null;
            if (category != null && !category.isEmpty()) {
                categoryType = CategoryType.valueOf(category.toUpperCase());
            }
            List<ServiceEntity> services = serviceService.searchWithFilters(keyword, categoryType, location, minPrice, maxPrice);
            return ResponseEntity.ok(services);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Récupérer les services disponibles
    @GetMapping("/available")
    public ResponseEntity<List<ServiceEntity>> getAvailableServices() {
        List<ServiceEntity> services = serviceService.getAvailableServices();
        return ResponseEntity.ok(services);
    }

    // Récupérer les services par type (FABRICATION, REPARATION, etc.)
    @GetMapping("/type/{serviceType}")
    public ResponseEntity<List<ServiceEntity>> getServicesByServiceType(@PathVariable String serviceType) {
        try {
            List<ServiceEntity> services = serviceService.getServicesByServiceType(serviceType);
            return ResponseEntity.ok(services);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}