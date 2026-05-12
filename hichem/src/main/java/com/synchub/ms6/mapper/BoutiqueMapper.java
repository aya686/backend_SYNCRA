package com.synchub.ms6.mapper;

import com.synchub.ms6.dto.BoutiqueDTOs;
import com.synchub.ms6.entity.Boutique;
import com.synchub.ms6.entity.Configuration;
import com.synchub.ms6.entity.StatsBoutique;

public class BoutiqueMapper {

    public static Boutique toEntity(BoutiqueDTOs.BoutiqueRequest dto) {
        return Boutique.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .theme(dto.getTheme())
                .logo(dto.getLogo())
                .build();
    }

    public static BoutiqueDTOs.BoutiqueResponse toResponse(Boutique entity) {
        return BoutiqueDTOs.BoutiqueResponse.builder()
                .boutiqueId(entity.getBoutiqueId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .theme(entity.getTheme())
                .logo(entity.getLogo())
                .dateCreation(entity.getDateCreation())
                .build();
    }

    public static Configuration toEntity(BoutiqueDTOs.ConfigurationRequest dto) {
        return Configuration.builder()
                .livraison(dto.getLivraison())
                .paiementAccepte(dto.getPaiementAccepte())
                .politique(dto.getPolitique())
                .langue(dto.getLangue())
                .build();
    }

    public static BoutiqueDTOs.ConfigurationResponse toResponse(Configuration entity) {
        return BoutiqueDTOs.ConfigurationResponse.builder()
                .configId(entity.getConfigId())
                .livraison(entity.getLivraison())
                .paiementAccepte(entity.getPaiementAccepte())
                .politique(entity.getPolitique())
                .langue(entity.getLangue())
                .build();
    }

    public static BoutiqueDTOs.StatsBoutiqueResponse toResponse(StatsBoutique entity) {
        return BoutiqueDTOs.StatsBoutiqueResponse.builder()
                .statsId(entity.getStatsId())
                .totalVentes(entity.getTotalVentes())
                .totalCommandes(entity.getTotalCommandes())
                .noteMoyenne(entity.getNoteMoyenne())
                .dateCalcul(entity.getDateCalcul())
                .build();
    }
}
