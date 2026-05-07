package com.synchub.ms6.converter;

import com.synchub.ms6.entity.PointLivraison;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convertisseur pour gérer les valeurs invalides ou vides du statut point de livraison
 * en les remplaçant par PLANIFIE par défaut.
 */
@Converter(autoApply = true)
@Slf4j
public class StatutPointConverter implements AttributeConverter<PointLivraison.StatutPoint, String> {

    @Override
    public String convertToDatabaseColumn(PointLivraison.StatutPoint statut) {
        if (statut == null) {
            return PointLivraison.StatutPoint.PLANIFIE.name();
        }
        return statut.name();
    }

    @Override
    public PointLivraison.StatutPoint convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            log.warn("Statut point vide/null en base, utilisation de PLANIFIE par défaut");
            return PointLivraison.StatutPoint.PLANIFIE;
        }
        try {
            return PointLivraison.StatutPoint.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Statut point invalide en base: '{}', utilisation de PLANIFIE par défaut", dbData);
            return PointLivraison.StatutPoint.PLANIFIE;
        }
    }
}
