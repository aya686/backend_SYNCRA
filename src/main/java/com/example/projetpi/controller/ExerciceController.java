package com.example.projetpi.controller;
import com.example.projetpi.entity.Exercice;
import com.example.projetpi.service.ExerciceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/exercice")
@CrossOrigin
public class ExerciceController {
    @Autowired
    private ExerciceService exerciceService;
    @PostMapping
    public Exercice create(@RequestBody Exercice exercice) {
        return exerciceService.create(exercice);
    }
    @GetMapping
    public List<Exercice> findAll() {
        return exerciceService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<Exercice> findById(@PathVariable Long id) {
        return exerciceService.findById(id);
    }
    @PutMapping("/{id}")
    public Exercice update(@PathVariable Long id, @RequestBody Exercice exercice) {
        return exerciceService.update(id, exercice);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        exerciceService.delete(id);
    }
}
