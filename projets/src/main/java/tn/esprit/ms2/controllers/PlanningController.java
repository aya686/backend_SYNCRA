package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Planning;
import tn.esprit.ms2.services.PlanningService;
import java.util.List;

@RestController
@RequestMapping("/api/plannings")
@RequiredArgsConstructor
@Tag(name = "Plannings")
public class PlanningController {

    private final PlanningService planningService;

    @GetMapping
    public List<Planning> getAll() { return planningService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Planning> getById(@PathVariable Long id) {
        return ResponseEntity.ok(planningService.getById(id));
    }

    @PostMapping("/projet/{projetId}/sprint/{sprintId}")
    public ResponseEntity<Planning> create(@PathVariable Long projetId, @PathVariable Long sprintId, @RequestBody Planning planning) {
        return ResponseEntity.ok(planningService.create(projetId, sprintId, planning));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Planning> update(@PathVariable Long id, @RequestBody Planning planning) {
        return ResponseEntity.ok(planningService.update(id, planning));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planningService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projet/{projetId}")
    public List<Planning> getByProjet(@PathVariable Long projetId) { return planningService.getByProjet(projetId); }
}