package com.example.projetpi.controller;
import com.example.projetpi.entity.Specialiste;
import com.example.projetpi.service.SpecialisteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/specialiste")
@CrossOrigin
public class SpecialisteController {
    @Autowired
    private SpecialisteService specialisteService;
    @PostMapping
    public Specialiste create(@RequestBody Specialiste specialiste) {
        return specialisteService.create(specialiste);
    }
    @GetMapping
    public List<Specialiste> findAll() {
        return specialisteService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<Specialiste> findById(@PathVariable Long id) {
        return specialisteService.findById(id);
    }
    @PutMapping("/{id}")
    public Specialiste update(@PathVariable Long id, @RequestBody Specialiste specialiste) {
        return specialisteService.update(id, specialiste);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        specialisteService.delete(id);
    }
}
