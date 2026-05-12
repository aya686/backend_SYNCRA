package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Affectation;
import tn.esprit.ms2.services.AffectationService;
import java.util.List;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
@Tag(name = "Affectations")
public class AffectationController {

    private final AffectationService affectationService;

    @GetMapping
    public List<Affectation> getAll() { return affectationService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Affectation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(affectationService.getById(id));
    }

    @PostMapping("/tache/{tacheId}")
    public ResponseEntity<Affectation> create(@PathVariable Long tacheId, @RequestBody Affectation affectation) {
        return ResponseEntity.ok(affectationService.create(tacheId, affectation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Affectation> update(@PathVariable Long id, @RequestBody Affectation affectation) {
        return ResponseEntity.ok(affectationService.update(id, affectation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        affectationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tache/{tacheId}")
    public List<Affectation> getByTache(@PathVariable Long tacheId) { return affectationService.getByTache(tacheId); }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<Affectation> getByUtilisateur(@PathVariable Long utilisateurId) { return affectationService.getByUtilisateur(utilisateurId); }
}