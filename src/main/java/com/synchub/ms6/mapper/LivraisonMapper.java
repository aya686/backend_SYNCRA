package com.synchub.ms6.mapper;

import com.synchub.ms6.dto.LivraisonDTOs;
import com.synchub.ms6.entity.*;

public class LivraisonMapper {

    public static Livraison toEntity(LivraisonDTOs.LivraisonRequest dto, Commande commande) {
        return Livraison.builder()
                .commande(commande)
                .adresse(dto.getAdresse())
                .transporteur(dto.getTransporteur())
                .tracking(dto.getTracking())
                .statut(Livraison.StatutLivraison.EN_PREPARATION)
                .build();
    }

    public static LivraisonDTOs.LivraisonResponse toResponse(Livraison entity) {
        return LivraisonDTOs.LivraisonResponse.builder()
                .livraisonId(entity.getLivraisonId())
                .commandeId(entity.getCommande() != null ? entity.getCommande().getCommandeId() : null)
                .adresse(entity.getAdresse())
                .transporteur(entity.getTransporteur())
                .tracking(entity.getTracking())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .dateExp(entity.getDateExp())
                .dateLiv(entity.getDateLiv())
                // Champs météo
                .weatherTemp(entity.getWeatherTemp())
                .weatherCondition(entity.getWeatherCondition())
                .weatherDescription(entity.getWeatherDescription())
                .weatherIcon(entity.getWeatherIcon())
                .weatherAlert(entity.getWeatherAlert())
                .build();
    }

    public static Retour toEntity(LivraisonDTOs.RetourRequest dto, Livraison livraison) {
        return Retour.builder()
                .livraison(livraison)
                .motif(dto.getMotif())
                .condition(dto.getCondition())
                .statut(Retour.StatutRetour.DEMANDE)
                .build();
    }

    public static LivraisonDTOs.RetourResponse toResponse(Retour entity) {
        return LivraisonDTOs.RetourResponse.builder()
                .retourId(entity.getRetourId())
                .livraisonId(entity.getLivraison() != null ? entity.getLivraison().getLivraisonId() : null)
                .motif(entity.getMotif())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .dateRetour(entity.getDateRetour())
                .condition(entity.getCondition())
                .build();
    }

    public static Remboursement toEntity(LivraisonDTOs.RemboursementRequest dto, Retour retour) {
        return Remboursement.builder()
                .retour(retour)
                .montant(dto.getMontant())
                .methode(Remboursement.MethodeRemboursement.valueOf(dto.getMethode()))
                .statut(Remboursement.StatutRemboursement.EN_ATTENTE)
                .build();
    }

    public static LivraisonDTOs.RemboursementResponse toResponse(Remboursement entity) {
        return LivraisonDTOs.RemboursementResponse.builder()
                .remboursementId(entity.getRemboursementId())
                .retourId(entity.getRetour() != null ? entity.getRetour().getRetourId() : null)
                .montant(entity.getMontant())
                .methode(entity.getMethode() != null ? entity.getMethode().name() : null)
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .dateTraitement(entity.getDateTraitement())
                .build();
    }
}
