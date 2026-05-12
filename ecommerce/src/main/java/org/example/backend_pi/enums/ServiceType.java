package org.example.backend_pi.enums;
// enums/ServiceType.java

import lombok.Getter;

@Getter
public enum ServiceType {
    FABRICATION("Fabrication"),
    REPARATION("Réparation"),
    CONSULTATION("Consultation"),
    LIVRAISON("Livraison"),
    AUTRE("Autre");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }
}
