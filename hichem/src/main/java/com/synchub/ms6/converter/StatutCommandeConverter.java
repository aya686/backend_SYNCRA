package com.synchub.ms6.converter;

import com.synchub.ms6.entity.Commande;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convertisseur pour gérer les valeurs invalides ou vides du statut commande
 * en les remplaçant par EN_ATTENTE par défaut.
 */
@Converter(autoApply = true)
@Slf4j
public class StatutCommandeConverter implements AttributeConverter<Commande.StatutCommande, String> {

    @Override
    public String convertToDatabaseColumn(Commande.StatutCommande statut) {
        if (statut == null) {
            return Commande.StatutCommande.EN_ATTENTE.name();
        }
        return statut.name();
    }

    @Override
    public Commande.StatutCommande convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            log.warn("Statut commande vide/null en base, utilisation de EN_ATTENTE par défaut");
            return Commande.StatutCommande.EN_ATTENTE;
        }
        try {
            return Commande.StatutCommande.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Statut commande invalide en base: '{}', utilisation de EN_ATTENTE par défaut", dbData);
            return Commande.StatutCommande.EN_ATTENTE;
        }
    }
}
