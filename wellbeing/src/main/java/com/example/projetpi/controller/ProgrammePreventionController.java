package com.example.projetpi.controller;

import com.example.projetpi.entity.ProgrammePrevention;
import com.example.projetpi.service.ProgrammePreventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/programmeprevention")
@CrossOrigin
public class ProgrammePreventionController {
    @Autowired
    private ProgrammePreventionService programmePreventionService;

    @PostMapping
    public ProgrammePrevention create(@RequestBody ProgrammePrevention programmePrevention) {
        return programmePreventionService.create(programmePrevention);
    }

    @GetMapping
    public List<ProgrammePrevention> findAll() {
        return programmePreventionService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ProgrammePrevention> findById(@PathVariable Long id) {
        return programmePreventionService.findById(id);
    }

    @PutMapping("/{id}")
    public ProgrammePrevention update(@PathVariable Long id, @RequestBody ProgrammePrevention programmePrevention) {
        return programmePreventionService.update(id, programmePrevention);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        programmePreventionService.delete(id);
    }
}
