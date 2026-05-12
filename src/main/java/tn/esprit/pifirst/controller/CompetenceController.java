package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Competence;
import tn.esprit.pifirst.service.CompetenceService;
import java.util.List;

@RestController
@RequestMapping("/api/competences")
public class CompetenceController {

    @Autowired
    private CompetenceService competenceService;

    @GetMapping
    public List<Competence> getAll() {
        return competenceService.getAll();
    }

    @GetMapping("/{id}")
    public Competence getById(@PathVariable Long id) {
        return competenceService.getById(id);
    }

    // AJOUTER CET ENDPOINT
    @GetMapping("/user/{idUser}")
    public List<Competence> getByUser(@PathVariable Long idUser) {
        return competenceService.getByUser(idUser);
    }

    // AJOUTER CET ENDPOINT
    @PostMapping("/user/{idUser}")
    public Competence create(@PathVariable Long idUser, @RequestBody Competence competence) {
        return competenceService.create(competence, idUser);
    }

    @PutMapping("/{id}")
    public Competence update(@PathVariable Long id, @RequestBody Competence competence) {
        return competenceService.update(id, competence);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        competenceService.delete(id);
    }
}