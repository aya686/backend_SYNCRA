package com.synchub.ms6.converter;

import com.synchub.ms6.entity.RouteLivraison;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convertisseur pour gérer les valeurs invalides ou vides du statut route
 * en les remplaçant par PLANIFIEE par défaut.
 */
@Converter(autoApply = true)
@Slf4j
public class StatutRouteConverter implements AttributeConverter<RouteLivraison.StatutRoute, String> {

    @Override
    public String convertToDatabaseColumn(RouteLivraison.StatutRoute statut) {
        if (statut == null) {
            return RouteLivraison.StatutRoute.PLANIFIEE.name();
        }
        return statut.name();
    }

    @Override
    public RouteLivraison.StatutRoute convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            log.warn("Statut route vide/null en base, utilisation de PLANIFIEE par défaut");
            return RouteLivraison.StatutRoute.PLANIFIEE;
        }
        try {
            return RouteLivraison.StatutRoute.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Statut route invalide en base: '{}', utilisation de PLANIFIEE par défaut", dbData);
            return RouteLivraison.StatutRoute.PLANIFIEE;
        }
    }
}
