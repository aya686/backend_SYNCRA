package com.example.projetpi.controller;

import com.example.projetpi.entity.Consultation;
import com.example.projetpi.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/consultation")
@CrossOrigin
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping
    public Consultation create(@RequestBody Consultation consultation) {
        return consultationService.create(consultation);
    }

    @GetMapping
    public List<Consultation> findAll() {
        return consultationService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Consultation> findById(@PathVariable Long id) {
        return consultationService.findById(id);
    }

    @PutMapping("/{id}")
    public Consultation update(@PathVariable Long id, @RequestBody Consultation consultation) {
        return consultationService.update(id, consultation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        consultationService.delete(id);
    }
}
