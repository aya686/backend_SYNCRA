package org.example.backend_pi.enums;
// enums/AvailabilityStatus.java

import lombok.Getter;

@Getter
public enum AvailabilityStatus {
    AVAILABLE("Disponible"),
    UNAVAILABLE("Indisponible"),
    RESERVED("Réservé");

    private final String label;

    AvailabilityStatus(String label) {
        this.label = label;
    }
}
