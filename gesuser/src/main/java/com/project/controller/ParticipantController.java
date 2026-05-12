package com.project.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.entity.Participant;
import com.project.service.ParticipantService;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    @Autowired
    private ParticipantService service;

    @GetMapping
    public List<Participant> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Participant create(@RequestBody Participant participant) {
        return service.save(participant);
    }

    @GetMapping("/{id}")
    public Participant getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Participant supprimé";
    }
}