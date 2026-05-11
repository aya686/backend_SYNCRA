package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.DTO.AnalyseIAResponse;
import tn.esprit.ms2.entities.Alerte;
import tn.esprit.ms2.entities.TypeAlerte;
import tn.esprit.ms2.services.AlerteService;
import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
@Tag(name = "Alertes")
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping
    public List<Alerte> getAll() { return alerteService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Alerte> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Alerte> create(@RequestBody Alerte alerte) {
        return ResponseEntity.ok(alerteService.createAndPublish(alerte));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alerte> update(@PathVariable Long id, @RequestBody Alerte alerte) {
        return ResponseEntity.ok(alerteService.update(id, alerte));
    }

    @PatchMapping("/{id}/traiter")
    @Operation(summary = "Marquer une alerte comme traitée")
    public ResponseEntity<Alerte> marquerTraitee(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.marquerTraitee(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alerteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<Alerte> getByUtilisateur(@PathVariable Long utilisateurId) { return alerteService.getByUtilisateur(utilisateurId); }

    @GetMapping("/projet/{projetId}")
    public List<Alerte> getByProjet(@PathVariable Long projetId) { return alerteService.getByProjet(projetId); }

    @GetMapping("/non-traitees")
    public List<Alerte> getNonTraitees() { return alerteService.getNonTraitees(); }

    @GetMapping("/type/{type}")
    public List<Alerte> getByType(@PathVariable TypeAlerte type) { return alerteService.getByType(type); }
    @GetMapping("/utilisateur/{utilisateurId}/non-traitees")
    public List<Alerte> getNonTraiteesParUtilisateur(@PathVariable Long utilisateurId) {
        return alerteService.getNonTraiteesParUtilisateur(utilisateurId);
    }
    // Analyse IA d'une alerte avec recommandations Groq
    @PostMapping("/{id}/analyser-ia")
    public ResponseEntity<AnalyseIAResponse> analyserAvecIA(@PathVariable Long id) {
        return ResponseEntity.ok(alerteService.analyserAvecIA(id));
    }

    // Vérifier si l'utilisateur a une alerte bloquante active
    @GetMapping("/utilisateur/{userId}/bloquante")
    public ResponseEntity<Alerte> getAlerteBloquante(@PathVariable Long userId) {
        return ResponseEntity.ok(alerteService.getAlerteBloquante(userId));
    }
}