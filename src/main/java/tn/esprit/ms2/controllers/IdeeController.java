package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Idee;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.services.IdeeService;
import java.util.List;

import tn.esprit.ms2.DTO.TransformationResultDTO;
@RestController
@RequestMapping("/api/idees")
@RequiredArgsConstructor
@Tag(name = "Idées")
public class IdeeController {

    private final IdeeService ideeService;

    @GetMapping
    public List<Idee>
    getAll() { return ideeService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Idee> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ideeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Idee> create(@RequestBody Idee idee) {
        return ResponseEntity.ok(ideeService.create(idee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Idee> update(@PathVariable Long id, @RequestBody Idee idee) {
        return ResponseEntity.ok(ideeService.update(id, idee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ideeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ideeId}/transformer")
    @Operation(summary = "Transformer une idée en projet")
    public ResponseEntity<TransformationResultDTO> transformer(
            @PathVariable Long ideeId,
            @RequestBody Projet projet) {
        return ResponseEntity.ok(ideeService.transformerEnProjet(ideeId, projet));
    }

    @GetMapping("/sans-projet")
    public List<Idee> getSansProjet() { return ideeService.getSansProjet(); }

    @GetMapping("/auteur/{auteurId}")
    public List<Idee> getByAuteur(@PathVariable Long auteurId) { return ideeService.getByAuteur(auteurId); }
}