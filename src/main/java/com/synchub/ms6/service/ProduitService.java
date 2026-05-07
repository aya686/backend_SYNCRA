package com.synchub.ms6.service;

import com.synchub.ms6.dto.ProduitDTOs;
import com.synchub.ms6.entity.*;
import com.synchub.ms6.mapper.ProduitMapper;
import com.synchub.ms6.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final StockRepository stockRepository;
    private final PromotionRepository promotionRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    @Transactional
    public ProduitDTOs.ProduitResponse addProduit(ProduitDTOs.ProduitRequest request) {
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + request.getBoutiqueId()));

        Produit produit = ProduitMapper.toEntity(request);
        produit.setBoutique(boutique);
        produit.setImages(request.getImages()); // Fix: Save images

        // Handle stock if provided
        if (produit.getStock() != null) {
            produit.getStock().setProduit(produit);
        }

        Produit saved = produitRepository.save(produit);
        return ProduitMapper.toResponse(saved);
    }

    public List<ProduitDTOs.ProduitResponse> getAllProduits(Boolean actif, Long boutiqueId) {
        List<Produit> produits;

        if (actif != null && boutiqueId != null) {
            produits = produitRepository.findByBoutiqueBoutiqueIdAndActifTrue(boutiqueId);
        } else if (actif != null && actif) {
            produits = produitRepository.findByActifTrue();
        } else if (boutiqueId != null) {
            produits = produitRepository.findByBoutiqueBoutiqueId(boutiqueId);
        } else {
            produits = produitRepository.findAll();
        }

        return produits.stream()
                .map(ProduitMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProduitDTOs.ProduitResponse getProduitById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + id));
        return ProduitMapper.toResponse(produit);
    }

    @Transactional
    public ProduitDTOs.ProduitResponse updateProduit(Long id, ProduitDTOs.ProduitRequest request) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + id));

        produit.setNom(request.getNom());
        produit.setDescription(request.getDescription());
        produit.setPrix(request.getPrix());
        produit.setCategories(request.getCategories());
        produit.setImages(request.getImages());

        Produit updated = produitRepository.save(produit);
        return ProduitMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + id));

        log.info("Suppression produit {} - {}, Promotions: {}, Archive: {}",
                id, produit.getNom(), produit.getPromotions().size(), produit.getArchive());

        // Étape 0: Supprimer les lignes de commande associées (si elles existent)
        List<LigneCommande> lignesCommande = ligneCommandeRepository.findByProduitProduitId(id);
        if (!lignesCommande.isEmpty()) {
            log.info("Suppression de {} lignes de commande pour le produit {}", lignesCommande.size(), id);
            for (LigneCommande ligne : lignesCommande) {
                ligneCommandeRepository.delete(ligne);
            }
            log.info("Lignes de commande supprimées");
        }

        // Étape 1-3: Supprimer les données des tables optionnelles (isolé dans une autre transaction)
        deleteOptionalData(id);

        // Étape 4: Supprimer explicitement les relations dans la table de jointure
        if (!produit.getPromotions().isEmpty()) {
            log.info("Suppression des relations produit_promotion pour produit {}", id);
            produitRepository.deleteProduitPromotionRelations(id);
            log.info("Relations supprimées de la table de jointure");
        }

        // Étape 5: Vider la collection côté Java
        produit.getPromotions().clear();

        // Étape 6: Flush pour synchroniser
        produitRepository.flush();

        // Étape 7: Supprimer le stock associé s'il existe
        stockRepository.findByProduitProduitId(id).ifPresent(stock -> {
            log.info("Suppression du stock pour le produit {}", id);
            stockRepository.delete(stock);
        });

        // Étape 8: Supprimer le produit
        produitRepository.delete(produit);
        log.info("Produit {} supprimé avec succès", id);
    }

    /**
     * Supprime les données des tables optionnelles (user_behaviors, recommendation_logs, alertes_stock)
     * dans une transaction séparée pour ne pas compromettre la transaction principale.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOptionalData(Long produitId) {
        // Étape 1: Supprimer les comportements utilisateur (user_behaviors)
        try {
            log.info("Suppression des user_behaviors pour produit {}", produitId);
            produitRepository.deleteUserBehaviors(produitId);
            log.info("User behaviors supprimés");
        } catch (Exception e) {
            log.warn("Table user_behaviors non trouvée ou erreur: {}", e.getMessage());
        }

        // Étape 2: Supprimer les logs de recommandation
        try {
            log.info("Suppression des recommendation_logs pour produit {}", produitId);
            produitRepository.deleteRecommendationLogs(produitId);
            log.info("Recommendation logs supprimés");
        } catch (Exception e) {
            log.warn("Table recommendation_logs non trouvée ou erreur: {}", e.getMessage());
        }

        // Étape 3: Supprimer les alertes stock
        try {
            log.info("Suppression des alertes_stock pour produit {}", produitId);
            produitRepository.deleteAlertesStock(produitId);
            log.info("Alertes stock supprimées");
        } catch (Exception e) {
            log.warn("Table alertes_stock non trouvée ou erreur: {}", e.getMessage());
        }
    }

    @Transactional
    public ProduitDTOs.ProduitResponse archiveProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + id));

        // Archiver le produit - on ne touche PAS au champ actif
        produit.setArchive(true);
        Produit updated = produitRepository.save(produit);
        return ProduitMapper.toResponse(updated);
    }

    @Transactional
    public ProduitDTOs.ProduitResponse unarchiveProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + id));

        // Désarchiver le produit - on ne touche PAS au champ actif
        produit.setArchive(false);
        Produit updated = produitRepository.save(produit);
        return ProduitMapper.toResponse(updated);
    }

    public ProduitDTOs.StockResponse getStockByProduitId(Long produitId) {
        Stock stock = stockRepository.findByProduitProduitId(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Stock non trouvé pour le produit: " + produitId));
        return ProduitMapper.toResponse(stock);
    }

    @Transactional
    public ProduitDTOs.StockResponse updateStock(Long produitId, ProduitDTOs.StockRequest request) {
        Stock stock = stockRepository.findByProduitProduitId(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Stock non trouvé pour le produit: " + produitId));

        stock.setQuantite(request.getQuantite());
        if (request.getSeuilAlerte() != null) {
            stock.setSeuilAlerte(request.getSeuilAlerte());
        }
        stock.setEntrepot(request.getEntrepot());

        Stock updated = stockRepository.save(stock);
        return ProduitMapper.toResponse(updated);
    }

    // Business rule: Check stock alert
    public boolean isStockAlert(Long produitId) {
        return stockRepository.findByProduitProduitId(produitId)
                .map(Stock::isAlerteStock)
                .orElse(false);
    }

    @Transactional
    public ProduitDTOs.PromotionResponse createPromotion(ProduitDTOs.PromotionRequest request) {
        // Validate code promo uniqueness
        if (request.getCodePromo() != null && promotionRepository.existsByCodePromo(request.getCodePromo())) {
            throw new IllegalArgumentException("Code promo déjà existant: " + request.getCodePromo());
        }

        Promotion promotion = ProduitMapper.toEntity(request);
        Promotion saved = promotionRepository.save(promotion);
        return ProduitMapper.toResponse(saved);
    }

    public List<ProduitDTOs.PromotionResponse> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
                .map(ProduitMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProduitDTOs.PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(ProduitMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProduitDTOs.PromotionResponse updatePromotion(Long id, ProduitDTOs.PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée: " + id));

        // Update fields
        promotion.setType(Promotion.TypePromotion.valueOf(request.getType()));
        promotion.setValeur(request.getValeur());
        promotion.setDateDebut(request.getDateDebut());
        promotion.setDateFin(request.getDateFin());
        promotion.setCodePromo(request.getCodePromo());

        Promotion saved = promotionRepository.save(promotion);
        return ProduitMapper.toResponse(saved);
    }

    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée: " + id));
        
        log.info("Suppression de la promotion {} - Actif: {}", id, promotion.isActive());
        
        try {
            // Étape 1: Supprimer explicitement les relations dans la table de jointure (SQL natif)
            log.info("Suppression des relations dans produit_promotion pour promo {}", id);
            promotionRepository.deleteProduitPromotionRelations(id);
            log.info("Relations supprimées de la table de jointure");
            
            // Étape 2: Vider les collections côté Java pour synchroniser l'état
            promotion.getProduits().clear();
            
            // Étape 3: Flush pour synchroniser avec la base
            promotionRepository.flush();
            
            // Étape 4: Supprimer la promotion
            promotionRepository.delete(promotion);
            log.info("Promotion {} supprimée avec succès", id);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la promotion {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression de la promotion: " + e.getMessage());
        }
    }

    @Transactional
    public void applyPromotionToProduit(Long promoId, Long produitId) {
        Promotion promotion = promotionRepository.findById(promoId)
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée: " + promoId));

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + produitId));

        produit.getPromotions().add(promotion);
        produitRepository.save(produit);
    }
}
