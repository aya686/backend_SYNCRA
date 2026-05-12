package com.synchub.ms6.controller;

import com.synchub.ms6.dto.BoutiqueDTOs;
import com.synchub.ms6.service.BoutiqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ms6/api/boutiques")
@RequiredArgsConstructor
public class BoutiqueController {

    private final BoutiqueService boutiqueService;

    @PostMapping
    public ResponseEntity<BoutiqueDTOs.BoutiqueResponse> createBoutique(
            @Valid @RequestBody BoutiqueDTOs.BoutiqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boutiqueService.createBoutique(request));
    }

    @GetMapping
    public ResponseEntity<List<BoutiqueDTOs.BoutiqueResponse>> getAllBoutiques() {
        return ResponseEntity.ok(boutiqueService.getAllBoutiques());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoutiqueDTOs.BoutiqueResponse> getBoutiqueById(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.getBoutiqueById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoutiqueDTOs.BoutiqueResponse> updateBoutique(
            @PathVariable Long id,
            @Valid @RequestBody BoutiqueDTOs.BoutiqueRequest request) {
        return ResponseEntity.ok(boutiqueService.updateBoutique(id, request));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<BoutiqueDTOs.BoutiqueResponse> toggleSuspendBoutique(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.toggleSuspendBoutique(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoutique(@PathVariable Long id) {
        boutiqueService.deleteBoutique(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<BoutiqueDTOs.StatsBoutiqueResponse> getBoutiqueStats(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.getBoutiqueStats(id));
    }

    @PutMapping("/{id}/config")
    public ResponseEntity<BoutiqueDTOs.ConfigurationResponse> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody BoutiqueDTOs.ConfigurationRequest request) {
        return ResponseEntity.ok(boutiqueService.updateConfiguration(id, request));
    }

    @GetMapping("/{id}/config")
    public ResponseEntity<BoutiqueDTOs.ConfigurationResponse> getConfiguration(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.getConfiguration(id));
    }
}
