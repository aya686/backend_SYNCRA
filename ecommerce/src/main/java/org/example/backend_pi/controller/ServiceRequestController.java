package org.example.backend_pi.controller;
// controller/ServiceRequestController.java
// ✅ BUG 2 CORRIGÉ : NotificationService injecté + notifications envoyées
//    sur chaque changement de statut de demande

import org.example.backend_pi.entity.ServiceRequest;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.service.NotificationService;
import org.example.backend_pi.service.ServiceRequestService;
import org.example.backend_pi.dto.ServiceRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService requestService;

    // ✅ CORRECTION BUG 2 : injection de NotificationService
    @Autowired
    private NotificationService notificationService;

    // ─── CRUD ────────────────────────────────────────────────────

    /**
     * Créer une demande.
     * ✅ Envoie une notification au prestataire ciblé (s'il est dans l'app).
     */
    @PostMapping
    public ResponseEntity<ServiceRequest> addRequest(@RequestBody ServiceRequestDTO requestDTO) {
        try {
            ServiceRequest request = requestService.addRequest(requestDTO);

            // ✅ Notifier le prestataire inscrit qu'il a reçu une nouvelle demande
            if (!Boolean.TRUE.equals(request.getIsExternalProvider())
                    && request.getTargetProviderId() != null) {
                notificationService.create(
                        request.getTargetProviderId(),
                        NotificationType.REQUEST_RECEIVED,
                        "📩 Nouvelle demande reçue",
                        (request.getRequesterName() != null ? request.getRequesterName() : "Un utilisateur")
                                + " vous a envoyé une demande : \""
                                + request.getTitle() + "\"",
                        request.getId(), "REQUEST",
                        "/requests/" + request.getId()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequest> updateRequest(
            @PathVariable Long id,
            @RequestBody ServiceRequestDTO requestDTO) {
        try {
            ServiceRequest request = requestService.updateRequest(id, requestDTO);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

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

    // ─── LISTES ──────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ServiceRequest>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getRequestById(@PathVariable Long id) {
        ServiceRequest request = requestService.getRequestById(id);
        if (request == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(request);
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(requestService.getRequestsByRequester(requesterId));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(requestService.getRequestsByProvider(providerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }

    @GetMapping("/type/{requestType}")
    public ResponseEntity<List<ServiceRequest>> getRequestsByType(@PathVariable String requestType) {
        return ResponseEntity.ok(requestService.getRequestsByType(requestType));
    }

    // ─── CHANGEMENTS DE STATUT ───────────────────────────────────

    /**
     * Changer le statut d'une demande (admin ou prestataire).
     * ✅ Envoie une notification au demandeur selon le nouveau statut.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String responseMessage) {
        try {
            ServiceRequest request = requestService.updateStatus(id, status, responseMessage);

            // ✅ Notifier le demandeur selon le statut
            sendStatusNotification(request, status, responseMessage);

            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Répondre à une demande (le prestataire accepte avec une proposition de prix).
     * ✅ Envoie une notification "ACCEPTED" au demandeur.
     */
    @PostMapping("/{id}/respond")
    public ResponseEntity<ServiceRequest> respondToRequest(
            @PathVariable Long id,
            @RequestParam String responseMessage,
            @RequestParam(required = false) Double proposedPrice,
            @RequestParam(required = false) String estimatedDelivery) {
        try {
            ServiceRequest request = requestService.respondToRequest(id, responseMessage, proposedPrice, estimatedDelivery);

            // ✅ Notifier le demandeur que sa demande a été acceptée
            if (request.getRequesterId() != null) {
                String content = "Votre demande \"" + request.getTitle()
                        + "\" a été acceptée par le prestataire.";
                if (proposedPrice != null) {
                    content += " Prix proposé : " + proposedPrice + " TND.";
                }
                if (estimatedDelivery != null && !estimatedDelivery.isEmpty()) {
                    content += " Délai estimé : " + estimatedDelivery + ".";
                }

                notificationService.create(
                        request.getRequesterId(),
                        NotificationType.REQUEST_ACCEPTED,
                        "✅ Votre demande a été acceptée",
                        content,
                        request.getId(), "REQUEST",
                        "/requests/" + request.getId()
                );
            }

            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Annuler une demande.
     * ✅ Notifie les deux parties (demandeur + prestataire si inscrit).
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ServiceRequest> cancelRequest(@PathVariable Long id) {
        try {
            ServiceRequest request = requestService.cancelRequest(id);

            // Notifier le demandeur
            if (request.getRequesterId() != null) {
                notificationService.create(
                        request.getRequesterId(),
                        NotificationType.REQUEST_CANCELLED,
                        "🚫 Demande annulée",
                        "Votre demande \"" + request.getTitle() + "\" a été annulée.",
                        request.getId(), "REQUEST",
                        "/requests/" + request.getId()
                );
            }

            // Notifier le prestataire si inscrit dans l'app
            if (!Boolean.TRUE.equals(request.getIsExternalProvider())
                    && request.getTargetProviderId() != null) {
                notificationService.create(
                        request.getTargetProviderId(),
                        NotificationType.REQUEST_CANCELLED,
                        "🚫 Demande annulée",
                        "La demande \"" + request.getTitle() + "\" de "
                                + (request.getRequesterName() != null ? request.getRequesterName() : "l'utilisateur")
                                + " a été annulée.",
                        request.getId(), "REQUEST",
                        "/requests/" + request.getId()
                );
            }

            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── MÉTHODE UTILITAIRE NOTIFICATIONS ────────────────────────

    /**
     * ✅ Envoie la notification appropriée au demandeur selon le statut.
     * Appelée par updateStatus().
     */
    private void sendStatusNotification(ServiceRequest request, String status, String responseMessage) {
        if (request.getRequesterId() == null) return;

        NotificationType type;
        String notifTitle;
        String notifContent;

        switch (status.toUpperCase()) {
            case "ACCEPTED":
                type = NotificationType.REQUEST_ACCEPTED;
                notifTitle = "✅ Votre demande a été acceptée";
                notifContent = "Votre demande \"" + request.getTitle() + "\" a été acceptée.";
                break;

            case "REJECTED":
                type = NotificationType.REQUEST_REJECTED;
                notifTitle = "❌ Votre demande a été refusée";
                notifContent = "Votre demande \"" + request.getTitle() + "\" a été refusée.";
                if (responseMessage != null && !responseMessage.isEmpty()) {
                    notifContent += " Message : " + responseMessage;
                }
                break;

            case "IN_PROGRESS":
                type = NotificationType.REQUEST_IN_PROGRESS;
                notifTitle = "🔄 Votre demande est en cours";
                notifContent = "Votre demande \"" + request.getTitle()
                        + "\" est maintenant en cours de traitement.";
                break;

            case "COMPLETED":
                type = NotificationType.REQUEST_COMPLETED;
                notifTitle = "🎉 Votre demande est terminée";
                notifContent = "Votre demande \"" + request.getTitle()
                        + "\" a été complétée avec succès !";
                break;

            case "CANCELLED":
                type = NotificationType.REQUEST_CANCELLED;
                notifTitle = "🚫 Demande annulée";
                notifContent = "Votre demande \"" + request.getTitle() + "\" a été annulée.";
                break;

            default:
                // Pour les autres statuts (PENDING etc.) on n'envoie pas de notification
                return;
        }

        notificationService.create(
                request.getRequesterId(),
                type,
                notifTitle,
                notifContent,
                request.getId(), "REQUEST",
                "/requests/" + request.getId()
        );
    }
}