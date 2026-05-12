package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.Calendrier;
import tn.esprit.ms2.entities.TypeCalendrier;
import tn.esprit.ms2.services.CalendrierService;
import java.util.List;

@RestController
@RequestMapping("/api/calendrier")
@RequiredArgsConstructor
@Tag(name = "Calendrier")
public class CalendrierController {

    private final CalendrierService calendrierService;

    @GetMapping
    public List<Calendrier> getAll() { return calendrierService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Calendrier> getById(@PathVariable Long id) {
        return ResponseEntity.ok(calendrierService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Calendrier> create(@RequestBody Calendrier calendrier) {
        return ResponseEntity.ok(calendrierService.create(calendrier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Calendrier> update(@PathVariable Long id, @RequestBody Calendrier calendrier) {
        return ResponseEntity.ok(calendrierService.update(id, calendrier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        calendrierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projet/{projetId}")
    public List<Calendrier> getByProjet(@PathVariable Long projetId) { return calendrierService.getByProjet(projetId); }

    @GetMapping("/type/{type}")
    public List<Calendrier> getByType(@PathVariable TypeCalendrier type) { return calendrierService.getByType(type); }
}