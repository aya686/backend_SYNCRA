package com.synchub.ms6.mapper;

import com.synchub.ms6.dto.CommandeDTOs;
import com.synchub.ms6.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class CommandeMapper {

    public static Commande toEntity(CommandeDTOs.CommandeRequest dto) {
        return Commande.builder()
                .adresseLivraison(dto.getAdresseLivraison())
                .statut(Commande.StatutCommande.EN_ATTENTE)
                .build();
    }

    public static LigneCommande toEntity(CommandeDTOs.LigneCommandeRequest dto, Produit produit) {
        LigneCommande ligne = LigneCommande.builder()
                .produit(produit)
                .quantite(dto.getQuantite())
                .prixUnitaire(produit.getPrix())
                .build();
        ligne.calculerTotal();
        return ligne;
    }

    public static CommandeDTOs.CommandeResponse toResponse(Commande entity) {
        return CommandeDTOs.CommandeResponse.builder()
                .commandeId(entity.getCommandeId())
                .montantTotal(entity.getMontantTotal())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .date(entity.getDate())
                .adresseLivraison(entity.getAdresseLivraison())
                .lignes(entity.getLignes() != null ?
                        entity.getLignes().stream().map(CommandeMapper::toResponse).collect(Collectors.toList()) :
                        List.of())
                .codePromo(entity.getCodePromo())
                .montantAvantRemise(entity.getMontantAvantRemise())
                .montantRemise(entity.getMontantRemise())
                .promotionType(entity.getPromotion() != null && entity.getPromotion().getType() != null ? 
                        entity.getPromotion().getType().name() : null)
                .promotionValeur(entity.getPromotion() != null ? entity.getPromotion().getValeur() : null)
                .build();
    }

    public static CommandeDTOs.LigneCommandeResponse toResponse(LigneCommande entity) {
        return CommandeDTOs.LigneCommandeResponse.builder()
                .ligneId(entity.getLigneId())
                .produitId(entity.getProduit() != null ? entity.getProduit().getProduitId() : null)
                .nomProduit(entity.getProduit() != null ? entity.getProduit().getNom() : null)
                .quantite(entity.getQuantite())
                .prixUnitaire(entity.getPrixUnitaire())
                .total(entity.getTotal())
                .build();
    }

    public static Annulation toEntity(CommandeDTOs.AnnulationRequest dto, Commande commande) {
        return Annulation.builder()
                .commande(commande)
                .motif(dto.getMotif())
                .rembourse(dto.getRembourse())
                .statut(Annulation.StatutAnnulation.DEMANDEE)
                .build();
    }

    public static CommandeDTOs.AnnulationResponse toResponse(Annulation entity) {
        return CommandeDTOs.AnnulationResponse.builder()
                .annulationId(entity.getAnnulationId())
                .commandeId(entity.getCommande() != null ? entity.getCommande().getCommandeId() : null)
                .motif(entity.getMotif())
                .dateAnnulation(entity.getDateAnnulation())
                .rembourse(entity.getRembourse())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .build();
    }
}
