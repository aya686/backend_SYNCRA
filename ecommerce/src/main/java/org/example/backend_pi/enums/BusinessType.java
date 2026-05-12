package org.example.backend_pi.enums;

import lombok.Getter;

@Getter
public enum BusinessType {
    MANUFACTURER("Fabricant / Producteur"),
    CUSTOM_MANUFACTURER("Fabricant spécifique au client"),
    DISTRIBUTOR("Distributeur"),
    SERVICE_PROVIDER("Prestataire de services"),
    WHOLESALER("Grossiste");

    private final String label;

    BusinessType(String label) {
        this.label = label;
    }
}