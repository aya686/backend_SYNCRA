package org.example.backend_pi.enums;
// enums/TransactionType.java

import lombok.Getter;

@Getter
public enum TransactionType {
    SALE("À vendre"),
    RENT("À louer"),
    BOTH("À vendre ou à louer");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }
}
