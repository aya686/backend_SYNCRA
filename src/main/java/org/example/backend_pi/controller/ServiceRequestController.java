package org.example.backend_pi.controller;

// controller/ServiceRequestController.java

import org.example.backend_pi.entity.ServiceRequest;
import org.example.backend_pi.service.ServiceRequestService;
import org.example.backend_pi.dto.ServiceRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService requestService;

    // Ajouter une demande
    @PostMapping
    public ResponseEntity<ServiceRequest> addRequest(@RequestBody ServiceRequestDTO requestDTO) {
        try {
            ServiceRequest request = requestService.addRequest(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Modifier une demande
    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequest> updateRequest(@PathVariable Long id, @RequestBody ServiceRequestDTO requestDTO) {
        try {
            ServiceRequest request = requestService.updateRequest(id, requestDTO);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supprimer une demande (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id) {
        try {
            requestService.deleteRequest(id);
            return ResponseEntity.ok("Demande supprimée définitivement avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        }
    }

    // Récupérer toutes les demandes
    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllRequests() {
        List<ServiceRequest> requests = requestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    // Récupérer une demande par ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getRequestById(@PathVariable Long id) {
        ServiceRequest request = requestService.getRequestById(id);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(request);
    }

    // Récupérer les demandes d'un demandeur
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByRequester(@PathVariable Long requesterId) {
        List<ServiceRequest> requests = requestService.getRequestsByRequester(requesterId);
        return ResponseEntity.ok(requests);
    }

    // Récupérer les demandes d'un fournisseur
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByProvider(@PathVariable Long providerId) {
        List<ServiceRequest> requests = requestService.getRequestsByProvider(providerId);
        return ResponseEntity.ok(requests);
    }

    // Récupérer les demandes par statut
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByStatus(@PathVariable String status) {
        List<ServiceRequest> requests = requestService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    // Récupérer les demandes par type
    @GetMapping("/type/{requestType}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByType(@PathVariable String requestType) {
        List<ServiceRequest> requests = requestService.getRequestsByType(requestType);
        return ResponseEntity.ok(requests);
    }

    // Changer le statut d'une demande
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String responseMessage) {
        try {
            ServiceRequest request = requestService.updateStatus(id, status, responseMessage);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Répondre à une demande (accepter avec proposition de prix)
    @PostMapping("/{id}/respond")
    public ResponseEntity<ServiceRequest> respondToRequest(
            @PathVariable Long id,
            @RequestParam String responseMessage,
            @RequestParam(required = false) Double proposedPrice,
            @RequestParam(required = false) String estimatedDelivery) {
        try {
            ServiceRequest request = requestService.respondToRequest(id, responseMessage, proposedPrice, estimatedDelivery);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Annuler une demande
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ServiceRequest> cancelRequest(@PathVariable Long id) {
        try {
            ServiceRequest request = requestService.cancelRequest(id);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}