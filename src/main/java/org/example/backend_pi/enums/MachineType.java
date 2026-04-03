package org.example.backend_pi.enums;

// enums/MachineType.java

import lombok.Getter;

@Getter
public enum MachineType {
    EQUIPMENT("Équipement"),
    RAW_MATERIAL("Matière première"),
    PART_ACCESSORY("Pièce et accessoire");

    private final String label;

    MachineType(String label) {
        this.label = label;
    }
}