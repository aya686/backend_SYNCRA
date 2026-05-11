package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Avancement;
import tn.esprit.ms2.services.AvancementService;
import java.util.List;

@RestController
@RequestMapping("/api/avancements")
@RequiredArgsConstructor
@Tag(name = "Avancement")
public class AvancementController {

    private final AvancementService avancementService;

    @GetMapping
    public List<Avancement> getAll() { return avancementService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Avancement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(avancementService.getById(id));
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<Avancement> getByProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(avancementService.getByProjet(projetId));
    }

    @PostMapping("/projet/{projetId}")
    public ResponseEntity<Avancement> create(@PathVariable Long projetId, @RequestBody Avancement avancement) {
        return ResponseEntity.ok(avancementService.create(projetId, avancement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Avancement> update(@PathVariable Long id, @RequestBody Avancement avancement) {
        return ResponseEntity.ok(avancementService.update(id, avancement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        avancementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}