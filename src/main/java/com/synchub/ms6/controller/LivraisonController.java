package com.synchub.ms6.controller;

import com.synchub.ms6.dto.LivraisonDTOs;
import com.synchub.ms6.service.LivraisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ms6/api")
@RequiredArgsConstructor
public class LivraisonController {

    private final LivraisonService livraisonService;

    @PostMapping("/livraisons")
    public ResponseEntity<LivraisonDTOs.LivraisonResponse> createLivraison(
            @Valid @RequestBody LivraisonDTOs.LivraisonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livraisonService.createLivraison(request));
    }

    @GetMapping("/livraisons")
    public ResponseEntity<List<LivraisonDTOs.LivraisonResponse>> getAllLivraisons() {
        return ResponseEntity.ok(livraisonService.getAllLivraisons());
    }

    @GetMapping("/livraisons/{id}")
    public ResponseEntity<LivraisonDTOs.LivraisonResponse> getLivraisonById(@PathVariable Long id) {
        return ResponseEntity.ok(livraisonService.getLivraisonById(id));
    }

    @PutMapping("/livraisons/{id}")
    public ResponseEntity<LivraisonDTOs.LivraisonResponse> updateLivraison(
            @PathVariable Long id,
            @Valid @RequestBody LivraisonDTOs.LivraisonUpdateRequest request) {
        return ResponseEntity.ok(livraisonService.updateLivraison(id, request));
    }

    @DeleteMapping("/livraisons/{id}")
    public ResponseEntity<Void> deleteLivraison(@PathVariable Long id) {
        livraisonService.deleteLivraison(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/livraisons/{id}/statut")
    public ResponseEntity<LivraisonDTOs.LivraisonResponse> updateLivraisonStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        return ResponseEntity.ok(livraisonService.updateStatut(id, statut));
    }

    @PostMapping("/livraisons/{id}/retours")
    public ResponseEntity<LivraisonDTOs.RetourResponse> initiateRetour(
            @PathVariable Long id,
            @Valid @RequestBody LivraisonDTOs.RetourRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livraisonService.initiateRetour(id, request));
    }

    @GetMapping("/livraisons/{id}/retours")
    public ResponseEntity<List<LivraisonDTOs.RetourResponse>> getRetoursByLivraison(@PathVariable Long id) {
        return ResponseEntity.ok(livraisonService.getRetoursByLivraison(id));
    }

    @GetMapping("/retours/{id}")
    public ResponseEntity<LivraisonDTOs.RetourResponse> getRetourById(@PathVariable Long id) {
        return ResponseEntity.ok(livraisonService.getRetourById(id));
    }

    @PatchMapping("/retours/{id}/statut")
    public ResponseEntity<LivraisonDTOs.RetourResponse> updateRetourStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        return ResponseEntity.ok(livraisonService.updateRetourStatut(id, statut));
    }

    @PostMapping("/retours/{id}/remboursement")
    public ResponseEntity<LivraisonDTOs.RemboursementResponse> createRemboursement(
            @PathVariable Long id,
            @Valid @RequestBody LivraisonDTOs.RemboursementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livraisonService.createRemboursement(id, request));
    }

    @GetMapping("/retours/{id}/remboursement")
    public ResponseEntity<LivraisonDTOs.RemboursementResponse> getRemboursement(@PathVariable Long id) {
        return ResponseEntity.ok(livraisonService.getRemboursementByRetourId(id));
    }
}
