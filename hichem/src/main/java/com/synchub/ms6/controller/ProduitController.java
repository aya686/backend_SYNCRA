package com.synchub.ms6.controller;

import com.synchub.ms6.dto.ProduitDTOs;
import com.synchub.ms6.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ms6/api")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping("/produits")
    public ResponseEntity<ProduitDTOs.ProduitResponse> addProduit(
            @Valid @RequestBody ProduitDTOs.ProduitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.addProduit(request));
    }

    @GetMapping("/produits")
    public ResponseEntity<List<ProduitDTOs.ProduitResponse>> getAllProduits(
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Long boutique) {
        return ResponseEntity.ok(produitService.getAllProduits(actif, boutique));
    }

    @GetMapping("/produits/{id}")
    public ResponseEntity<ProduitDTOs.ProduitResponse> getProduitById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitById(id));
    }

    @PutMapping("/produits/{id}")
    public ResponseEntity<ProduitDTOs.ProduitResponse> updateProduit(
            @PathVariable Long id,
            @Valid @RequestBody ProduitDTOs.ProduitRequest request) {
        return ResponseEntity.ok(produitService.updateProduit(id, request));
    }

    @DeleteMapping("/produits/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/produits/{id}/archive")
    public ResponseEntity<ProduitDTOs.ProduitResponse> archiveProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.archiveProduit(id));
    }

    @PatchMapping("/produits/{id}/unarchive")
    public ResponseEntity<ProduitDTOs.ProduitResponse> unarchiveProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.unarchiveProduit(id));
    }

    @GetMapping("/produits/{id}/stock")
    public ResponseEntity<ProduitDTOs.StockResponse> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getStockByProduitId(id));
    }

    @PutMapping("/produits/{id}/stock")
    public ResponseEntity<ProduitDTOs.StockResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody ProduitDTOs.StockRequest request) {
        return ResponseEntity.ok(produitService.updateStock(id, request));
    }

    @PostMapping("/promotions")
    public ResponseEntity<ProduitDTOs.PromotionResponse> createPromotion(
            @Valid @RequestBody ProduitDTOs.PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.createPromotion(request));
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<ProduitDTOs.PromotionResponse>> getActivePromotions() {
        return ResponseEntity.ok(produitService.getActivePromotions());
    }

    @GetMapping("/promotions/all")
    public ResponseEntity<List<ProduitDTOs.PromotionResponse>> getAllPromotions() {
        return ResponseEntity.ok(produitService.getAllPromotions());
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<ProduitDTOs.PromotionResponse> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody ProduitDTOs.PromotionRequest request) {
        return ResponseEntity.ok(produitService.updatePromotion(id, request));
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        produitService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/promotions/{id}/apply/{produitId}")
    public ResponseEntity<Void> applyPromotionToProduit(
            @PathVariable Long id,
            @PathVariable Long produitId) {
        produitService.applyPromotionToProduit(id, produitId);
        return ResponseEntity.ok().build();
    }
}
