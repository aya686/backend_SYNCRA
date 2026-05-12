package tn.esprit.ms2.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.DTO.SessionTravailDTO;
import tn.esprit.ms2.entities.SessionTravail;
import tn.esprit.ms2.services.SessionTravailService;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions de Travail")
public class SessionTravailController {

    private final SessionTravailService sessionService;

    @GetMapping
    public List<SessionTravail> getAll() { return sessionService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<SessionTravail> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    @PostMapping("/tache/{tacheId}")
    public ResponseEntity<SessionTravail> create(@PathVariable Long tacheId, @RequestBody SessionTravail session) {
        return ResponseEntity.ok(sessionService.create(tacheId, session));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionTravail> update(@PathVariable Long id, @RequestBody SessionTravail session) {
        return ResponseEntity.ok(sessionService.update(id, session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<SessionTravail> create(@RequestBody SessionTravailDTO dto) {
        return ResponseEntity.ok(sessionService.create(dto));
    }


    @PatchMapping("/{id}/terminer")
    public ResponseEntity<SessionTravail> terminer(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.terminer(id));
    }

    @GetMapping("/tache/{tacheId}")
    public List<SessionTravail> getByTache(@PathVariable Long tacheId) { return sessionService.getByTache(tacheId); }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<SessionTravail> getByUtilisateur(@PathVariable Long utilisateurId) { return sessionService.getByUtilisateur(utilisateurId); }
}