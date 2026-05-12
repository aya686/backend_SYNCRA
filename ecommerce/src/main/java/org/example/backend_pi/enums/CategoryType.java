package org.example.backend_pi.enums;

// enums/CategoryType.java


import lombok.Getter;

@Getter
public enum CategoryType {

    INDUSTRIELLE("Industrielle", "Machines et équipements industriels"),
    DOMESTIQUE("Domestique", "Appareils et équipements domestiques"),
    AGRICOLE("Agricole", "Machines et équipements agricoles"),
    ELECTRONIQUE("Électronique", "Composants et équipements électroniques"),
    MEDICAL("Médical", "Équipements médicaux et paramédicaux"),
    BUREAUTIQUE("Bureautique", "Matériel de bureau et informatique"),
    CONSTRUCTION("Construction", "Matériel de construction et BTP"),
    AUTRE("Autre", "Autres catégories");

    private final String label;
    private final String description;

    CategoryType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}