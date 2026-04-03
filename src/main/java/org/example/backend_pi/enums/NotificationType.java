package org.example.backend_pi.enums;

// enums/NotificationType.java


import lombok.Getter;

@Getter
public enum NotificationType {
    NEW_REQUEST("Nouvelle demande"),
    REQUEST_ACCEPTED("Demande acceptée"),
    REQUEST_REJECTED("Demande refusée"),
    REQUEST_RESPONSE("Réponse à la demande"),
    REQUEST_COMPLETED("Demande terminée"),
    REQUEST_CANCELLED("Demande annulée");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }
}