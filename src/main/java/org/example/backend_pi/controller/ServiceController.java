package org.example.backend_pi.controller;

// controller/ServiceController.java

import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.service.ServiceService;
import org.example.backend_pi.service.NotificationService;
import org.example.backend_pi.dto.ServiceDTO;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private NotificationService notificationService;

    // ─── CRUD ───────────────────────────────────────────────────

    /** Tout user (freelancer/admin) peut créer un service */
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

    /**
     * Modifier un service.
     * - Admin  : peut modifier tout service
     * - Freelancer : modifie seulement ses services (vérifié côté frontend via providerId)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(
            @PathVariable Long id,
            @RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceEntity service = serviceService.updateService(id, serviceDTO);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Supprimer — admin peut tout supprimer */
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

    // ─── LISTES ─────────────────────────────────────────────────

    /** Retourne TOUS les services (admin + propriétaire voient leurs pending/rejected) */
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    /** Vue publique : seulement APPROVED + isInApp */
    @GetMapping("/public")
    public ResponseEntity<List<ServiceEntity>> getPublicServices() {
        List<ServiceEntity> services = serviceService.getApprovedServices().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsInApp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        ServiceEntity service = serviceService.getServiceById(id);
        if (service == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(service);
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceEntity>> getServicesByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(serviceService.getServicesByProvider(providerId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ServiceEntity>> getServicesByCategory(@PathVariable String category) {
        try {
            CategoryType cat = CategoryType.valueOf(category.toUpperCase());
            return ResponseEntity.ok(serviceService.getServicesByCategory(cat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceEntity>> searchServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            CategoryType cat = null;
            if (category != null && !category.isEmpty())
                cat = CategoryType.valueOf(category.toUpperCase());
            return ResponseEntity.ok(serviceService.searchWithFilters(keyword, cat, location, minPrice, maxPrice));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<ServiceEntity>> getAvailableServices() {
        return ResponseEntity.ok(serviceService.getAvailableServices());
    }

    @GetMapping("/type/{serviceType}")
    public ResponseEntity<List<ServiceEntity>> getServicesByServiceType(@PathVariable String serviceType) {
        try {
            return ResponseEntity.ok(serviceService.getServicesByServiceType(serviceType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── VALIDATION ADMIN ────────────────────────────────────────

    /** ✅ APPROUVER — PATCH /api/services/{id}/approve */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ServiceEntity> approveService(@PathVariable Long id) {
        try {
            ServiceEntity service = serviceService.approveService(id);

            // ✅ Envoyer une notification au prestataire
            if (notificationService != null && service.getProviderId() != null) {
                notificationService.create(
                        service.getProviderId(),
                        NotificationType.SERVICE_APPROVED,
                        "✅ Votre service a été approuvé",
                        "Votre service \"" + service.getName() + "\" est maintenant visible par tous les utilisateurs.",
                        service.getId(),
                        "SERVICE",
                        "/services/" + service.getId()
                );
            }

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** ✅ REJETER — PATCH /api/services/{id}/reject */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ServiceEntity> rejectService(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String reason = payload.getOrDefault("reason", "");
            ServiceEntity service = serviceService.rejectService(id, reason);

            // ✅ Envoyer une notification au prestataire avec la raison du rejet
            if (notificationService != null && service.getProviderId() != null) {
                String message = "Votre service \"" + service.getName() + "\" a été rejeté.";
                if (reason != null && !reason.isEmpty()) {
                    message += " Raison : " + reason;
                }

                notificationService.create(
                        service.getProviderId(),
                        NotificationType.SERVICE_REJECTED,
                        "❌ Votre service a été rejeté",
                        message,
                        service.getId(),
                        "SERVICE",
                        "/services/edit/" + service.getId()
                );
            }

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ServiceEntity>> getPendingServices() {
        return ResponseEntity.ok(serviceService.getPendingServices());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ServiceEntity>> getApprovedServices() {
        return ResponseEntity.ok(serviceService.getApprovedServices());
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<ServiceEntity>> getRejectedServices() {
        return ResponseEntity.ok(serviceService.getRejectedServices());
    }
}