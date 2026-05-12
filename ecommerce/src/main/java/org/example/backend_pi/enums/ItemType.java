package org.example.backend_pi.enums;

// enums/ItemType.java

import lombok.Getter;

@Getter
public enum ItemType {
    MACHINE("Machine");


    private final String label;

    ItemType(String label) {
        this.label = label;
    }
}