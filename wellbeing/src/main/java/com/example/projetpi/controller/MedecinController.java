package com.example.projetpi.controller;

import com.example.projetpi.entity.Medecin;
import com.example.projetpi.service.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/medecin")
@CrossOrigin
public class MedecinController {

    @Autowired
    private MedecinService medecinService;

    @PostMapping
    public Medecin create(@RequestBody Medecin medecin) {
        return medecinService.create(medecin);
    }

    @GetMapping
    public List<Medecin> findAll() {
        return medecinService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Medecin> findById(@PathVariable Long id) {
        return medecinService.findById(id);
    }

    @PutMapping("/{id}")
    public Medecin update(@PathVariable Long id, @RequestBody Medecin medecin) {
        return medecinService.update(id, medecin);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        medecinService.delete(id);
    }
}
