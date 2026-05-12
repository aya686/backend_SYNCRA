package com.project.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.entity.Moduleformation;
import com.project.service.ModuleformationService;

@RestController
@RequestMapping("/api/modules")
public class ModuleformationController {

    @Autowired
    private ModuleformationService service;

    @GetMapping
    public List<Moduleformation> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Moduleformation create(@RequestBody Moduleformation module) {
        return service.save(module);
    }

    @GetMapping("/{id}")
    public Moduleformation getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Moduleformation update(@PathVariable Long id, @RequestBody Moduleformation moduleDetails) {
        return service.update(id, moduleDetails);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Module supprimé avec succès !";
    }
}