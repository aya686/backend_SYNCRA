// src/main/java/com/project/controller/TrainingDataController.java
package com.project.controller;

import com.project.dto.TrainingDataDTO;
import com.project.entity.TrainingData;
import com.project.repository.TrainingDataRepository;
import com.project.service.TrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training-data")
@CrossOrigin(origins = "http://localhost:4200")
public class TrainingDataController {

    @Autowired
    private TrainingDataService service;
    @Autowired
    private TrainingDataRepository repository;

    // Enregistrer une nouvelle donnée d'entraînement
    @PostMapping
    public ResponseEntity<TrainingData> create(@RequestBody TrainingDataDTO dto) {
        TrainingData saved = service.save(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Récupérer toutes les données d'entraînement
    @GetMapping
    public ResponseEntity<List<TrainingData>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Récupérer les statistiques des données d'entraînement
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", service.count());
        stats.put("positiveChoices", service.countPositiveChoices());
        stats.put("negativeChoices", service.count() - service.countPositiveChoices());
        return ResponseEntity.ok(stats);
    }

    // Supprimer toutes les données (utile pour reset)
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }
    // TrainingDataController.java - Ajoutez cet endpoint
    @GetMapping("/stats/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.count());
        stats.put("byUserType", repository.findAll().stream()
                .collect(Collectors.groupingBy(TrainingData::getUserType, Collectors.counting())));
        stats.put("byEvent", repository.findAll().stream()
                .collect(Collectors.groupingBy(TrainingData::getEventId, Collectors.counting())));
        stats.put("positiveRate", (double) repository.findAll().stream()
                .filter(TrainingData::getDidChoose).count() / repository.count() * 100);
        return ResponseEntity.ok(stats);
    }

}