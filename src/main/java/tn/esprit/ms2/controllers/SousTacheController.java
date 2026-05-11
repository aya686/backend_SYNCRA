package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.SousTache;
import tn.esprit.ms2.services.SousTacheService;
import java.util.List;

@RestController
@RequestMapping("/api/sous-taches")
@RequiredArgsConstructor
@Tag(name = "Sous-Tâches")
public class SousTacheController {

    private final SousTacheService sousTacheService;

    @GetMapping
    public List<SousTache> getAll() { return sousTacheService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<SousTache> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sousTacheService.getById(id));
    }

    @PostMapping("/tache/{tacheId}")
    public ResponseEntity<SousTache> create(@PathVariable Long tacheId, @RequestBody SousTache sousTache) {
        return ResponseEntity.ok(sousTacheService.create(tacheId, sousTache));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SousTache> update(@PathVariable Long id, @RequestBody SousTache sousTache) {
        return ResponseEntity.ok(sousTacheService.update(id, sousTache));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sousTacheService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tache/{tacheId}")
    public List<SousTache> getByTache(@PathVariable Long tacheId) { return sousTacheService.getByTache(tacheId); }
}