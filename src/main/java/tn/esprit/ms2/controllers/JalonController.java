package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Jalon;
import tn.esprit.ms2.services.JalonService;
import tn.esprit.ms2.repositories.JalonRepository  ;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jalons")
@RequiredArgsConstructor
@Tag(name = "Jalons")
public class JalonController {

    private final JalonService jalonService;
    private final JalonRepository jalonRepository;

    @GetMapping
    public List<Jalon> getAll() { return jalonService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Jalon> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jalonService.getById(id));
    }

    @PostMapping("/projet/{projetId}")
    public ResponseEntity<Jalon> create(@PathVariable Long projetId, @RequestBody Jalon jalon) {
        return ResponseEntity.ok(jalonService.create(projetId, jalon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jalon> update(@PathVariable Long id, @RequestBody Jalon jalon) {
        return ResponseEntity.ok(jalonService.update(id, jalon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jalonService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/atteint")
    public ResponseEntity<Jalon> updateAtteint(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Jalon j = jalonService.getById(id);
        j.setAtteint(body.get("atteint"));
        return ResponseEntity.ok(jalonRepository.save(j));
    }
    @GetMapping("/projet/{projetId}")
    public List<Jalon> getByProjet(@PathVariable Long projetId) {
        return jalonService.getByProjet(projetId);
    }
}