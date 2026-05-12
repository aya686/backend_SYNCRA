package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Avancement;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.entities.StatutProjet;
import tn.esprit.ms2.repositories.AvancementRepository;
import tn.esprit.ms2.services.ProjetService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@Tag(name = "Projets")
public class ProjetController {

    private final ProjetService projetService;
    private final AvancementRepository avancementRepository; // ajoute ça


    @GetMapping
    @Operation(summary = "Lister tous les projets")
    public List<Projet> getAll() { return projetService.getAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un projet par ID")
    public ResponseEntity<Projet> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un projet")
    public ResponseEntity<Projet> create(@RequestBody Projet projet) {
        return ResponseEntity.ok(projetService.create(projet));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un projet")
    public ResponseEntity<Projet> update(@PathVariable Long id, @RequestBody Projet projet) {
        return ResponseEntity.ok(projetService.update(id, projet));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un projet")
    public ResponseEntity<Map<String, String>> deleteProjet(@PathVariable Long id) {
        projetService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Projet supprimé avec succès"));
    }
    @GetMapping("/{id}/avancement")
    public ResponseEntity<Avancement> getAvancement(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getAvancement(id)); // ← utilise le service
    }
    @PatchMapping("/{id}/assigner-moniteur")
    public ResponseEntity<Projet> assignerMoniteur(
            @PathVariable Long id,
            @RequestParam Long moniteurId) {
        Projet projet = projetService.assignerMoniteur(id, moniteurId);
        return ResponseEntity.ok(projet);
    }

    @GetMapping("/moniteur/{moniteurId}")
    public ResponseEntity<List<Projet>> getProjetsMoniteur(@PathVariable Long moniteurId) {
        return ResponseEntity.ok(projetService.getProjetsParMoniteur(moniteurId));
    }

    @PostMapping("/{id}/avancement/recalculer")
    @Operation(summary = "Recalculer l'avancement")
    public ResponseEntity<Avancement> recalculer(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.recalculerAvancement(id));
    }
    @GetMapping("/porteur/{porteurId}")
    @Operation(summary = "Projets par porteur")
    public List<Projet> getByPorteur(@PathVariable Long porteurId) {
        return projetService.getByPorteur(porteurId);
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Projets par statut")
    public List<Projet> getByStatut(@PathVariable StatutProjet statut) {
        return projetService.getByStatut(statut);
    }
}
