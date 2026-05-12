package com.synchub.ms6.controller;

import com.synchub.ms6.dto.CommandeDTOs;
import com.synchub.ms6.service.CommandeService;
import com.synchub.ms6.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ms6/api")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final SmsService smsService;

    @PostMapping("/commandes")
    public ResponseEntity<CommandeDTOs.CommandeResponse> placeOrder(
            @Valid @RequestBody CommandeDTOs.CommandeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.placeOrder(request));
    }

    @GetMapping("/commandes")
    public ResponseEntity<List<CommandeDTOs.CommandeResponse>> getAllCommandes(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(commandeService.getAllCommandes(statut, startDate, endDate));
    }

    @GetMapping("/commandes/{id}")
    public ResponseEntity<CommandeDTOs.CommandeResponse> getCommandeById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }

    @PatchMapping("/commandes/{id}/statut")
    public ResponseEntity<CommandeDTOs.CommandeResponse> updateStatut(
            @PathVariable Long id,
            @Valid @RequestBody CommandeDTOs.StatutUpdateRequest request) {
        return ResponseEntity.ok(commandeService.updateStatut(id, request.getStatut()));
    }

    @DeleteMapping("/commandes/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        commandeService.deleteCommande(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/commandes/{id}/cancel")
    public ResponseEntity<Void> cancelCommande(@PathVariable Long id) {
        commandeService.cancelCommande(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/commandes/{id}/annuler")
    public ResponseEntity<CommandeDTOs.AnnulationResponse> createAnnulation(
            @PathVariable Long id,
            @Valid @RequestBody CommandeDTOs.AnnulationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.createAnnulation(id, request));
    }

    @GetMapping("/commandes/{id}/annulation")
    public ResponseEntity<CommandeDTOs.AnnulationResponse> getAnnulation(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getAnnulationByCommandeId(id));
    }

    @PostMapping("/commandes/validate-promo")
    public ResponseEntity<CommandeDTOs.PromoValidationResponse> validatePromoCode(
            @RequestParam String codePromo,
            @RequestParam Double montantTotal) {
        return ResponseEntity.ok(commandeService.validatePromoCode(codePromo, montantTotal));
    }

    // Endpoint de test pour diagnostiquer les problèmes SMS (GET pour tester depuis navigateur)
    @GetMapping("/test-sms")
    public ResponseEntity<String> testSms(
            @RequestParam(defaultValue = "+21696619052") String to,
            @RequestParam(defaultValue = "Test SMS from SyncHub") String message) {
        smsService.sendSms(to, message);
        return ResponseEntity.ok("SMS test envoyé à " + to + ". Vérifiez les logs pour le statut.");
    }
}
