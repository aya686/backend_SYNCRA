package com.synchub.ms6.mapper;

import com.synchub.ms6.dto.ProduitDTOs;
import com.synchub.ms6.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public class ProduitMapper {

    public static Produit toEntity(ProduitDTOs.ProduitRequest dto) {
        Produit produit = Produit.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .categories(dto.getCategories())
                .images(dto.getImages())
                .actif(true)
                .archive(false)
                .build();

        if (dto.getStock() != null) {
            Stock stock = toEntity(dto.getStock());
            stock.setProduit(produit);
            produit.setStock(stock);
        }

        return produit;
    }

    public static ProduitDTOs.ProduitResponse toResponse(Produit entity) {
        Double prixPromo = entity.getPrix();
        if (entity.getPromotions() != null) {
            for (Promotion promo : entity.getPromotions()) {
                if (promo.isActive()) {
                    prixPromo = promo.calculerPrixPromo(prixPromo);
                }
            }
        }

        return ProduitDTOs.ProduitResponse.builder()
                .produitId(entity.getProduitId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .prix(entity.getPrix())
                .prixPromo(prixPromo < entity.getPrix() ? prixPromo : null)
                .categories(entity.getCategories())
                .images(entity.getImages())
                .actif(entity.getActif())
                .archive(entity.getArchive())
                .boutiqueId(entity.getBoutique() != null ? entity.getBoutique().getBoutiqueId() : null)
                .stock(entity.getStock() != null ? toResponse(entity.getStock()) : null)
                .promotionIds(entity.getPromotions() != null ?
                        entity.getPromotions().stream().map(Promotion::getPromoId).collect(Collectors.toList()) :
                        List.of())
                .build();
    }

    public static Stock toEntity(ProduitDTOs.StockRequest dto) {
        return Stock.builder()
                .quantite(dto.getQuantite())
                .seuilAlerte(dto.getSeuilAlerte() != null ? dto.getSeuilAlerte() : 10)
                .entrepot(dto.getEntrepot())
                .build();
    }

    public static ProduitDTOs.StockResponse toResponse(Stock entity) {
        return ProduitDTOs.StockResponse.builder()
                .stockId(entity.getStockId())
                .quantite(entity.getQuantite())
                .seuilAlerte(entity.getSeuilAlerte())
                .entrepot(entity.getEntrepot())
                .alerteStock(entity.isAlerteStock())
                .build();
    }

    public static Promotion toEntity(ProduitDTOs.PromotionRequest dto) {
        return Promotion.builder()
                .type(dto.getType() != null ? Promotion.TypePromotion.valueOf(dto.getType()) : null)
                .valeur(dto.getValeur())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .codePromo(dto.getCodePromo())
                .build();
    }

    public static ProduitDTOs.PromotionResponse toResponse(Promotion entity) {
        return ProduitDTOs.PromotionResponse.builder()
                .promoId(entity.getPromoId())
                .type(entity.getType() != null ? entity.getType().name() : null)
                .valeur(entity.getValeur())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .codePromo(entity.getCodePromo())
                .active(entity.isActive())
                .build();
    }
}
