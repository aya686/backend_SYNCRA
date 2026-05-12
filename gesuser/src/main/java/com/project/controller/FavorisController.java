// com/project/controller/FavorisController.java
package com.project.controller;

import com.project.entity.Favoris;
import com.project.repository.FavorisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoris")
@CrossOrigin(origins = "http://localhost:4200")
public class FavorisController {

    @Autowired
    private FavorisRepository repository;

    // Récupérer tous les favoris d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favoris>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(repository.findByUserId(userId));
    }

    // Ajouter un favori
    @PostMapping
    public ResponseEntity<Favoris> add(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long evenementId = body.get("evenementId");

        // Vérifier si existe déjà
        if (repository.existsByUserIdAndEvenementId(userId, evenementId)) {
            return ResponseEntity.badRequest().build();
        }

        Favoris favoris = new Favoris();
        favoris.setUserId(userId);
        favoris.setEvenementId(evenementId);
        favoris.setDateAjout(LocalDateTime.now());

        return ResponseEntity.ok(repository.save(favoris));
    }

    // Supprimer un favori
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}