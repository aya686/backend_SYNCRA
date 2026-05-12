package com.synchub.ms6.service;

import com.synchub.ms6.dto.CommandeDTOs;
import com.synchub.ms6.entity.*;
import com.synchub.ms6.mapper.CommandeMapper;
import com.synchub.ms6.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final AnnulationRepository annulationRepository;
    private final AlerteStockService alerteStockService;
    private final PromotionRepository promotionRepository;
    private final SmsService smsService;

    @Transactional
    public CommandeDTOs.CommandeResponse placeOrder(CommandeDTOs.CommandeRequest request) {
        Commande commande = CommandeMapper.toEntity(request);
        commande.setLignes(new ArrayList<>());

        // Process each ligne
        for (CommandeDTOs.LigneCommandeRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + ligneReq.getProduitId()));

            // Check and decrement stock
            Stock stock = stockRepository.findByProduitProduitId(produit.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Stock non trouvé pour le produit: " + produit.getProduitId()));

            if (stock.getQuantite() < ligneReq.getQuantite()) {
                throw new IllegalStateException("Stock insuffisant pour le produit: " + produit.getNom());
            }

            int quantiteAvant = stock.getQuantite();
            stock.setQuantite(stock.getQuantite() - ligneReq.getQuantite());
            stockRepository.save(stock);

            // Vérifier et envoyer alerte si nécessaire
            alerteStockService.verifierEtEnvoyerAlerte(stock, quantiteAvant);

            // Create ligne commande
            LigneCommande ligne = CommandeMapper.toEntity(ligneReq, produit);
            ligne.setCommande(commande);
            commande.getLignes().add(ligne);
        }

        // Calculate total
        commande.calculerMontantTotal();
        
        // Appliquer le code promo si fourni
        if (request.getCodePromo() != null && !request.getCodePromo().isEmpty()) {
            applyPromotion(commande, request.getCodePromo());
        }

        Commande saved = commandeRepository.save(commande);
        
        // Envoyer SMS de confirmation avec les détails de la promotion
        String adminPhone = smsService.getAdminPhone();
        if (saved.getPromotion() != null) {
            smsService.sendOrderConfirmationWithPromo(
                adminPhone,
                saved.getCommandeId(),
                saved.getMontantAvantRemise(),
                saved.getMontantRemise(),
                saved.getMontantTotal(),
                saved.getCodePromo()
            );
        } else {
            smsService.sendOrderConfirmation(adminPhone, saved.getCommandeId(), saved.getMontantTotal());
        }

        return CommandeMapper.toResponse(saved);
    }

    private void applyPromotion(Commande commande, String codePromo) {
        Promotion promotion = promotionRepository.findByCodePromo(codePromo)
                .orElseThrow(() -> new IllegalArgumentException("Code promo invalide: " + codePromo));
        
        if (!promotion.isActive()) {
            throw new IllegalStateException("Ce code promo a expiré ou n'est pas encore actif");
        }
        
        Double montantOriginal = commande.getMontantTotal();
        Double montantRemise;
        
        if (promotion.getType() == Promotion.TypePromotion.POURCENTAGE) {
            montantRemise = montantOriginal * (promotion.getValeur() / 100);
        } else {
            montantRemise = promotion.getValeur();
        }
        
        // S'assurer que le montant après remise n'est pas négatif
        Double montantFinal = Math.max(0, montantOriginal - montantRemise);
        
        commande.setMontantAvantRemise(montantOriginal);
        commande.setMontantRemise(montantRemise);
        commande.setMontantTotal(montantFinal);
        commande.setPromotion(promotion);
        commande.setCodePromo(codePromo);
    }
    
    public CommandeDTOs.PromoValidationResponse validatePromoCode(String codePromo, Double montantTotal) {
        if (codePromo == null || codePromo.isEmpty()) {
            return CommandeDTOs.PromoValidationResponse.builder()
                    .valid(false)
                    .message("Code promo requis")
                    .build();
        }
        
        var promotionOpt = promotionRepository.findByCodePromo(codePromo);
        if (promotionOpt.isEmpty()) {
            return CommandeDTOs.PromoValidationResponse.builder()
                    .valid(false)
                    .message("Code promo invalide")
                    .build();
        }
        
        Promotion promotion = promotionOpt.get();
        
        if (!promotion.isActive()) {
            return CommandeDTOs.PromoValidationResponse.builder()
                    .valid(false)
                    .message("Ce code promo a expiré ou n'est pas encore actif")
                    .build();
        }
        
        Double montantRemise;
        if (promotion.getType() == Promotion.TypePromotion.POURCENTAGE) {
            montantRemise = montantTotal * (promotion.getValeur() / 100);
        } else {
            montantRemise = Math.min(promotion.getValeur(), montantTotal);
        }
        
        Double montantApresRemise = Math.max(0, montantTotal - montantRemise);
        
        return CommandeDTOs.PromoValidationResponse.builder()
                .valid(true)
                .message("Code promo appliqué avec succès!")
                .codePromo(codePromo)
                .type(promotion.getType().name())
                .valeur(promotion.getValeur())
                .montantRemise(montantRemise)
                .montantAvantRemise(montantTotal)
                .montantApresRemise(montantApresRemise)
                .build();
    }

    public List<CommandeDTOs.CommandeResponse> getAllCommandes(String statut, LocalDateTime startDate, LocalDateTime endDate) {
        List<Commande> commandes;

        // Vérifier si le statut est vide ou invalide
        Commande.StatutCommande status = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                status = Commande.StatutCommande.valueOf(statut.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Statut invalide reçu: '{}' - Ignoré", statut);
                // Si le statut est invalide, on ignore le filtre
            }
        }

        if (status != null && startDate != null && endDate != null) {
            commandes = commandeRepository.findByStatutAndDateBetween(status, startDate, endDate);
        } else if (status != null) {
            commandes = commandeRepository.findByStatut(status);
        } else if (startDate != null && endDate != null) {
            commandes = commandeRepository.findByDateBetween(startDate, endDate);
        } else {
            commandes = commandeRepository.findAll();
        }

        return commandes.stream()
                .map(CommandeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CommandeDTOs.CommandeResponse getCommandeById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + id));
        return CommandeMapper.toResponse(commande);
    }

    @Transactional
    public CommandeDTOs.CommandeResponse updateStatut(Long id, String statut) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + id));

        Commande.StatutCommande newStatut = Commande.StatutCommande.valueOf(statut);
        commande.setStatut(newStatut);

        Commande updated = commandeRepository.save(commande);
        return CommandeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteCommande(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + id));
        
        log.info("Suppression commande {} - Statut: {}, Livraisons: {}, Lignes: {}, Annulation: {}",
                id, commande.getStatut(), 
                commande.getLivraisons().size(), 
                commande.getLignes().size(),
                commande.getAnnulation() != null);
        
        // Vérifier si la commande a des livraisons associées
        if (!commande.getLivraisons().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une commande avec des livraisons associées. Supprimez d'abord les livraisons.");
        }
        
        // Récupérer une copie des lignes avant modification
        List<LigneCommande> lignes = new ArrayList<>(commande.getLignes());
        
        // Vider la liste des lignes de la commande (détachement)
        commande.getLignes().clear();
        commandeRepository.save(commande);
        
        // Supprimer l'annulation si elle existe
        if (commande.getAnnulation() != null) {
            log.info("Suppression de l'annulation pour la commande {}", id);
            annulationRepository.delete(commande.getAnnulation());
            commande.setAnnulation(null);
        }
        
        // Restaurer le stock (seulement si la commande n'était pas annulée)
        if (commande.getStatut() != Commande.StatutCommande.ANNULEE) {
            for (LigneCommande ligne : lignes) {
                try {
                    Stock stock = stockRepository.findByProduitProduitId(ligne.getProduit().getProduitId())
                            .orElse(null);
                    if (stock != null) {
                        stock.setQuantite(stock.getQuantite() + ligne.getQuantite());
                        stockRepository.save(stock);
                        alerteStockService.envoyerNotificationRestock(stock, ligne.getQuantite());
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors de la restauration du stock pour le produit {}: {}", 
                            ligne.getProduit().getProduitId(), e.getMessage());
                }
            }
        }
        
        // Supprimer les lignes de commande une par une
        for (LigneCommande ligne : lignes) {
            try {
                ligneCommandeRepository.delete(ligne);
            } catch (Exception e) {
                log.error("Erreur suppression ligne {}: {}", ligne.getLigneId(), e.getMessage());
                throw new RuntimeException("Erreur lors de la suppression de la ligne de commande: " + e.getMessage());
            }
        }
        
        // Supprimer la commande
        commandeRepository.delete(commande);
        log.info("Commande {} supprimée avec succès", id);
    }

    @Transactional
    public void cancelCommande(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + id));

        if (!commande.canBeCancelled()) {
            throw new IllegalStateException("Cette commande ne peut pas être annulée (statut: " + commande.getStatut() + ")");
        }

        // Restore stock
        for (LigneCommande ligne : commande.getLignes()) {
            Stock stock = stockRepository.findByProduitProduitId(ligne.getProduit().getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Stock non trouvé"));
            int quantiteAvant = stock.getQuantite();
            stock.setQuantite(stock.getQuantite() + ligne.getQuantite());
            stockRepository.save(stock);
            
            // Envoyer notification de restock
            alerteStockService.envoyerNotificationRestock(stock, ligne.getQuantite());
        }

        commande.setStatut(Commande.StatutCommande.ANNULEE);
        commandeRepository.save(commande);
    }

    @Transactional
    public CommandeDTOs.AnnulationResponse createAnnulation(Long commandeId, CommandeDTOs.AnnulationRequest request) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + commandeId));

        if (!commande.canBeCancelled()) {
            throw new IllegalStateException("Cette commande ne peut pas être annulée");
        }

        Annulation annulation = CommandeMapper.toEntity(request, commande);
        Annulation saved = annulationRepository.save(annulation);

        // If rembourse is true, set commande to cancelled
        if (Boolean.TRUE.equals(request.getRembourse())) {
            commande.setStatut(Commande.StatutCommande.ANNULEE);
            commandeRepository.save(commande);
        }

        return CommandeMapper.toResponse(saved);
    }

    public CommandeDTOs.AnnulationResponse getAnnulationByCommandeId(Long commandeId) {
        Annulation annulation = annulationRepository.findByCommandeCommandeId(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Annulation non trouvée pour la commande: " + commandeId));
        return CommandeMapper.toResponse(annulation);
    }
}
