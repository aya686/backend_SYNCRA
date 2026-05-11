package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Objectif;
import tn.esprit.ms2.repositories.AvancementRepository;
import tn.esprit.ms2.services.ObjectifService;
import java.util.List;
import java.util.Map;
import tn.esprit.ms2.repositories.ObjectifRepository;

@RestController
@RequestMapping("/api/objectifs")
@RequiredArgsConstructor
@Tag(name = "Objectifs")
public class ObjectifController {

    private final ObjectifService objectifService;
    private final ObjectifRepository objectifRepository; // ajoute ça


    @GetMapping
    public List<Objectif> getAll() { return objectifService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Objectif> getById(@PathVariable Long id) {
        return ResponseEntity.ok(objectifService.getById(id));
    }

    @PostMapping("/projet/{projetId}")
    public ResponseEntity<Objectif> create(@PathVariable Long projetId, @RequestBody Objectif objectif) {
        return ResponseEntity.ok(objectifService.create(projetId, objectif));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objectif> update(@PathVariable Long id, @RequestBody Objectif objectif) {
        return ResponseEntity.ok(objectifService.update(id, objectif));
    }
    @PatchMapping("/{id}/atteint")
    public ResponseEntity<Objectif> updateAtteint(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Objectif o = objectifService.getById(id);
        o.setAtteint(body.get("atteint"));
        return ResponseEntity.ok(objectifRepository.save(o));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        objectifService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projet/{projetId}")
    public List<Objectif> getByProjet(@PathVariable Long projetId) {
        return objectifService.getByProjet(projetId);
    }
}