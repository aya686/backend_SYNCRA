package org.example.backend_pi.enums;

// enums/RequestType.java


import lombok.Getter;

@Getter
public enum RequestType {
    FABRICATION("Fabrication"),
    SERVICE("Service"),
    MACHINE_RENT("Location machine"),
    MACHINE_PURCHASE("Achat machine"),
    REPARATION("Réparation"),
    CONSULTATION("Consultation");

    private final String label;

    RequestType(String label) {
        this.label = label;
    }
}