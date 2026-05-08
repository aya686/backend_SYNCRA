package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Etudiant;
import tn.esprit.pifirst.service.EtudiantService;
import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    @Autowired
    private EtudiantService etudiantService;

    @GetMapping
    public List<Etudiant> getAll() {
        return etudiantService.getAll();
    }

    @GetMapping("/{id}")
    public Etudiant getById(@PathVariable Long id) {
        return etudiantService.getById(id);
    }

    @GetMapping("/acces-expires")
    public List<Etudiant> getAccesExpires() {
        return etudiantService.getAccesExpires();
    }

    @PostMapping("/user/{idUser}")
    public Etudiant create(@PathVariable Long idUser, @RequestBody Etudiant etudiant) {
        return etudiantService.create(etudiant, idUser);
    }

    @PutMapping("/{id}")
    public Etudiant update(@PathVariable Long id, @RequestBody Etudiant etudiant) {
        return etudiantService.update(id, etudiant);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        etudiantService.delete(id);
    }
}