package com.example.projetpi.controller;
import com.example.projetpi.entity.Rapport;
import com.example.projetpi.service.RapportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/rapport")
@CrossOrigin
public class RapportController {
    @Autowired
    private RapportService rapportService;
    @PostMapping
    public Rapport create(@RequestBody Rapport rapport) {
        return rapportService.create(rapport);
    }
    @GetMapping
    public List<Rapport> findAll() {
        return rapportService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<Rapport> findById(@PathVariable Long id) {
        return rapportService.findById(id);
    }
    @PutMapping("/{id}")
    public Rapport update(@PathVariable Long id, @RequestBody Rapport rapport) {
        return rapportService.update(id, rapport);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rapportService.delete(id);
    }
}
