package com.project.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.entity.Formateur;
import com.project.service.FormateurService;

@RestController
@RequestMapping("/api/formateurs")
public class FormateurController {

    @Autowired
    private FormateurService service;

    @GetMapping
    public List<Formateur> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Formateur create(@RequestBody Formateur formateur) {
        return service.save(formateur);
    }

    @GetMapping("/{id}")
    public Formateur getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Formateur update(@PathVariable Long id, @RequestBody Formateur formateurDetails) {
        return service.update(id, formateurDetails);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Formateur supprimé avec succès !";
    }
}