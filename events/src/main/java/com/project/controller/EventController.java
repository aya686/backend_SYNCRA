package com.project.controller;

import com.project.dto.EventDTO;
import com.project.entity.Event;
import com.project.entity.Inscription;
import com.project.repository.InscriptionRepository;
import com.project.service.EventService;
import com.project.service.InscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    @Autowired
    private EventService service;
    @Autowired
    private InscriptionRepository inscriptionRepository;

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAll() {
        List<EventDTO> events = service.getAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getById(@PathVariable Long id) {
        EventDTO event = service.getById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<EventDTO> create(@RequestBody EventDTO eventDTO) {
        EventDTO created = service.save(eventDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> update(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        EventDTO updated = service.update(id, eventDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @Autowired
    private InscriptionService inscriptionService;

    @GetMapping("/{id}/inscrits")
    public ResponseEntity<Long> getNbInscrits(@PathVariable Long id) {
        long count = inscriptionService.getNbInscritsByEventId(id);
        return ResponseEntity.ok(count);
    }
    // Dans EventController.java

    // Dans EventController.java

    @GetMapping("/{id}/popularite")
    public ResponseEntity<Map<String, Object>> getEventPopularite(@PathVariable Long id) {
        Event event = service.getByIdEntity(id);
        long nbInscrits = inscriptionService.getNbInscritsByEventId(id);
        double popularite = event.getCapacite() > 0 ? (double) nbInscrits / event.getCapacite() : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("popularite", popularite);
        response.put("nbInscrits", nbInscrits);
        response.put("capacite", event.getCapacite());

        return ResponseEntity.ok(response);
    }

    // EventController.java - AJOUTER CET ENDPOINT

    // EventController.java - CORRIGEZ CET ENDPOINT

    @GetMapping("/stats/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapData() {
        // Récupérer toutes les inscriptions avec leurs dates
        List<Inscription> inscriptions = inscriptionRepository.findAll();

        // Grouper par jour de semaine et heure
        Map<String, Integer> heatmapData = new HashMap<>();

        for (Inscription inscription : inscriptions) {
            if (inscription.getDateInscription() != null) {
                LocalDateTime date = inscription.getDateInscription();
                int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche
                int hour = date.getHour();
                String key = dayOfWeek + "-" + hour;
                heatmapData.put(key, heatmapData.getOrDefault(key, 0) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : heatmapData.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Map<String, Object> data = new HashMap<>();
            data.put("day", Integer.parseInt(parts[0]));
            data.put("hour", Integer.parseInt(parts[1]));
            data.put("count", entry.getValue());
            result.add(data);
        }

        // ✅ CORRECTION : Retourner List directement
        return ResponseEntity.ok(result);
    }}