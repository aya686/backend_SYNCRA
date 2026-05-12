package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.*;
import tn.esprit.ms2.services.TacheService;
import java.util.List;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
@Tag(name = "Tâches")
public class TacheController {

    private final TacheService tacheService;

    @GetMapping
    public List<Tache> getAll() { return tacheService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Tache> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getById(id));
    }

    @PostMapping("/projet/{projetId}")
    public ResponseEntity<Tache> create(@PathVariable Long projetId, @RequestBody Tache tache) {
        return ResponseEntity.ok(tacheService.create(projetId, tache));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tache> update(@PathVariable Long id, @RequestBody Tache tache) {
        return ResponseEntity.ok(tacheService.update(id, tache));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tacheService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projet/{projetId}")
    public List<Tache> getByProjet(@PathVariable Long projetId) { return tacheService.getByProjet(projetId); }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Tache> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutTache statut) {
        Tache tache = tacheService.changerStatut(id, statut);
        return ResponseEntity.ok(tache);
    }
    @GetMapping("/{id}/sous-taches")
    public ResponseEntity<List<SousTache>> getSousTaches(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getSousTaches(id));
    }

    @PatchMapping("/sous-taches/{id}/statut")
    public ResponseEntity<SousTache> changerStatutSousTache(
            @PathVariable Long id,
            @RequestParam StatutSousTache statut) {
        return ResponseEntity.ok(tacheService.changerStatutSousTache(id, statut));
    }
    @GetMapping("/priorite/{priorite}")
    public List<Tache> getByPriorite(@PathVariable PrioriteTache priorite) { return tacheService.getByPriorite(priorite); }
}