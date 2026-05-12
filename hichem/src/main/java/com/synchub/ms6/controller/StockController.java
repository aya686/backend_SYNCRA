package com.synchub.ms6.controller;

import com.synchub.ms6.entity.AlerteStock;
import com.synchub.ms6.entity.Stock;
import com.synchub.ms6.repository.StockRepository;
import com.synchub.ms6.service.AlerteStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ms6/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockRepository stockRepository;
    private final AlerteStockService alerteStockService;

    /**
     * Récupère tous les stocks en alerte (quantité <= seuil)
     */
    @GetMapping("/alertes")
    public ResponseEntity<List<Stock>> getStocksEnAlerte() {
        List<Stock> stocksEnAlerte = stockRepository.findAllStocksEnAlerte();
        return ResponseEntity.ok(stocksEnAlerte);
    }

    /**
     * Récupère les stocks complètement épuisés
     */
    @GetMapping("/epuises")
    public ResponseEntity<List<Stock>> getStocksEpuises() {
        List<Stock> stocksEpuises = stockRepository.findAllStocksEpuises();
        return ResponseEntity.ok(stocksEpuises);
    }

    /**
     * Récupère un résumé des stocks (total, en alerte, épuisés)
     */
    @GetMapping("/resume")
    public ResponseEntity<Map<String, Object>> getResumeStocks() {
        long total = stockRepository.count();
        Long enAlerte = stockRepository.countStocksEnAlerte();
        Long epuises = stockRepository.countStocksEpuises();
        
        Map<String, Object> resume = Map.of(
            "totalProduits", total,
            "stocksEnAlerte", enAlerte != null ? enAlerte : 0,
            "stocksEpuises", epuises != null ? epuises : 0,
            "stocksOk", total - (enAlerte != null ? enAlerte : 0)
        );
        
        return ResponseEntity.ok(resume);
    }

    /**
     * Récupère les stocks en alerte pour une boutique spécifique
     */
    @GetMapping("/alertes/boutique/{boutiqueId}")
    public ResponseEntity<List<Stock>> getStocksEnAlerteByBoutique(@PathVariable Long boutiqueId) {
        List<Stock> stocksEnAlerte = stockRepository.findStocksEnAlerteByBoutique(boutiqueId);
        return ResponseEntity.ok(stocksEnAlerte);
    }

    /**
     * Modifie le seuil d'alerte d'un produit
     */
    @PutMapping("/{produitId}/seuil")
    public ResponseEntity<Stock> updateSeuilAlerte(
            @PathVariable Long produitId,
            @RequestParam Integer nouveauSeuil) {
        Stock stock = stockRepository.findByProduitProduitId(produitId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour le produit: " + produitId));
        
        stock.setSeuilAlerte(nouveauSeuil);
        Stock updated = stockRepository.save(stock);
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Réapprovisionne un stock
     */
    @PutMapping("/{produitId}/reapprovisionner")
    public ResponseEntity<Stock> reapprovisionner(
            @PathVariable Long produitId,
            @RequestParam Integer quantite) {
        Stock stock = stockRepository.findByProduitProduitId(produitId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour le produit: " + produitId));
        
        int quantiteAvant = stock.getQuantite();
        stock.setQuantite(stock.getQuantite() + quantite);
        Stock updated = stockRepository.save(stock);
        
        // Envoyer notification de restock
        alerteStockService.envoyerNotificationRestock(updated, quantite);
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Teste la configuration email
     */
    @PostMapping("/test-email")
    public ResponseEntity<String> testerEmail() {
        try {
            alerteStockService.testerEmail();
            return ResponseEntity.ok("Email de test envoyé avec succès!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupère l'historique des alertes d'un produit
     */
    @GetMapping("/{produitId}/alertes-historique")
    public ResponseEntity<List<AlerteStock>> getHistoriqueAlertes(@PathVariable Long produitId) {
        List<AlerteStock> alertes = alerteStockService.getAlertesByProduit(produitId);
        return ResponseEntity.ok(alertes);
    }

    /**
     * Récupère les alertes récentes (7 derniers jours)
     */
    @GetMapping("/alertes-recentes")
    public ResponseEntity<List<AlerteStock>> getAlertesRecentes() {
        List<AlerteStock> alertes = alerteStockService.getAlertesRecentes();
        return ResponseEntity.ok(alertes);
    }
}
