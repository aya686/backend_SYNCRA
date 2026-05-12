package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Sprint;
import tn.esprit.ms2.entities.StatutSprint;
import tn.esprit.ms2.services.SprintService;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints")
public class SprintController {

    private final SprintService sprintService;

    @GetMapping
    public List<Sprint> getAll() { return sprintService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.getById(id));
    }

    @PostMapping("/projet/{projetId}")
    public ResponseEntity<Sprint> create(@PathVariable Long projetId, @RequestBody Sprint sprint) {
        return ResponseEntity.ok(sprintService.create(projetId, sprint));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        return ResponseEntity.ok(sprintService.update(id, sprint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projet/{projetId}")
    public List<Sprint> getByProjet(@PathVariable Long projetId) { return sprintService.getByProjet(projetId); }

    @GetMapping("/statut/{statut}")
    public List<Sprint> getByStatut(@PathVariable StatutSprint statut) { return sprintService.getByStatut(statut); }
}