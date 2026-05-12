package org.example.backend_pi.enums;
import lombok.Getter;

@Getter
public enum DeliveryType {
    STANDARD("Standard", 5.0),
    EXPRESS("Express", 15.0),
    SAME_DAY("Same day", 25.0);

    private final String label;
    private final double defaultCost;

    DeliveryType(String label, double defaultCost) {
        this.label = label;
        this.defaultCost = defaultCost;
    }
}