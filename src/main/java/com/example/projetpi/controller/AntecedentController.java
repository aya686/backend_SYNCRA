package com.example.projetpi.controller;

import com.example.projetpi.entity.Antecedent;
import com.example.projetpi.service.AntecedentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/antecedent")
@CrossOrigin
public class AntecedentController {

    @Autowired
    private AntecedentService antecedentService;

    @PostMapping
    public Antecedent create(@RequestBody Antecedent antecedent) {
        return antecedentService.create(antecedent);
    }

    @GetMapping
    public List<Antecedent> findAll() {
        return antecedentService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Antecedent> findById(@PathVariable Long id) {
        return antecedentService.findById(id);
    }

    @PutMapping("/{id}")
    public Antecedent update(@PathVariable Long id, @RequestBody Antecedent antecedent) {
        return antecedentService.update(id, antecedent);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        antecedentService.delete(id);
    }
}
