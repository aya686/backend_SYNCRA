package org.example.backend_pi.enums;

// enums/RequestStatus.java


import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    REJECTED("Refusée"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }
}