package com.project.service;

import com.project.dto.EventDTO;
import com.project.entity.Event;
import com.project.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository repo;

    public List<EventDTO> getAll() {
        return repo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getById(Long id) {
        Event event = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event non trouvé avec id: " + id));
        return convertToDTO(event);
    }

    public EventDTO save(EventDTO dto) {
        Event event = convertToEntity(dto);
        Event saved = repo.save(event);
        return convertToDTO(saved);
    }

    public EventDTO update(Long id, EventDTO dto) {
        Event event = repo.findById(id).orElseThrow(() -> new RuntimeException("Event non trouvé avec id: " + id));
        event.setDescription(dto.getDescription());
        event.setTitre(dto.getTitre());
        event.setType(dto.getType());
        event.setLieu(dto.getLieu());
        event.setDateHeure(dto.getDateHeure());
        event.setDateFin(dto.getDateFin());
        event.setCapacite(dto.getCapacite());
        event.setPrix(dto.getPrix());
        event.setStatut(dto.getStatut());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setImageUrl(dto.getImageUrl());  // ← AJOUTER CETTE LIGNE IMPORTANTE !

        Event updated = repo.save(event);
        return convertToDTO(updated);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    // Convertir Entity vers DTO
    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setDescription(event.getDescription());
        dto.setEvenementId(event.getEvenementId());
        dto.setTitre(event.getTitre());
        dto.setType(event.getType());
        dto.setLieu(event.getLieu());
        dto.setDateHeure(event.getDateHeure());
        dto.setDateFin(event.getDateFin());
        dto.setCapacite(event.getCapacite());
        dto.setPrix(event.getPrix());
        dto.setStatut(event.getStatut());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setImageUrl(event.getImageUrl());  // ← AJOUTER CETTE LIGNE
        return dto;
    }

    // Convertir DTO vers Entity
    private Event convertToEntity(EventDTO dto) {
        Event event = new Event();
        event.setEvenementId(dto.getEvenementId());
        event.setTitre(dto.getTitre());
        event.setType(dto.getType());
        event.setLieu(dto.getLieu());
        event.setDateHeure(dto.getDateHeure());
        event.setDateFin(dto.getDateFin());
        event.setCapacite(dto.getCapacite());
        event.setDescription(dto.getDescription());
        event.setPrix(dto.getPrix());
        event.setStatut(dto.getStatut());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setImageUrl(dto.getImageUrl());  // ← AJOUTER CETTE LIGNE
        return event;
    }
    // Dans EventService.java - Ajoutez cette méthode

    public Event getByIdEntity(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event non trouvé avec id: " + id));
    }
}