package com.project.controller;

import com.project.repository.EventRepository;
import com.project.repository.InscriptionRepository;
import com.project.repository.ParticipantRepository;
import com.project.repository.FormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private FormationRepository formationRepository;

    // 1. Évolution des inscriptions par mois
    @GetMapping("/evolution")
    public List<Map<String, Object>> getEvolution() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"};

        for (int i = 1; i <= 12; i++) {
            LocalDateTime start = LocalDateTime.of(2024, i, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long inscriptions = inscriptionRepository.countByDateBetween(start, end);
            long participants = inscriptionRepository.countParticipantsByDateBetween(start, end);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", mois[i-1]);
            monthData.put("inscriptions", inscriptions);
            monthData.put("participants", participants);
            result.add(monthData);
        }
        return result;
    }

    // 2. Répartition par type d'événement
    @GetMapping("/by-type")
    public Map<String, Long> getStatsByType() {
        List<Object[]> results = inscriptionRepository.countInscriptionsByEventType();
        Map<String, Long> typeStats = new HashMap<>();

        for (Object[] row : results) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            typeStats.put(type, count);
        }
        return typeStats;
    }

    // 3. Participants par jour de semaine
    @GetMapping("/by-weekday")
    public Map<String, Long> getStatsByWeekday() {
        List<Object[]> results = inscriptionRepository.countParticipantsByWeekday();
        Map<String, Long> weekdayStats = new LinkedHashMap<>();
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        // Initialiser tous les jours à 0
        for (String jour : jours) {
            weekdayStats.put(jour, 0L);
        }

        for (Object[] row : results) {
            int dayOfWeek = (int) row[0];
            Long count = (Long) row[1];
            if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                weekdayStats.put(jours[dayOfWeek - 1], count);
            }
        }
        return weekdayStats;
    }

    // 4. Heatmap (participants par jour et heure)
    @GetMapping("/heatmap")
    public List<Map<String, Object>> getHeatmap() {
        List<Object[]> results = inscriptionRepository.countParticipantsByDayAndHour();
        List<Map<String, Object>> heatmapData = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("day", row[0]);
            data.put("hour", row[1]);
            data.put("count", row[2]);
            heatmapData.add(data);
        }
        return heatmapData;
    }

    // 5. Statistiques globales
    @GetMapping("/global")
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.count());
        stats.put("totalFormations", formationRepository.count());
        stats.put("totalParticipants", participantRepository.count());
        stats.put("totalInscriptions", inscriptionRepository.count());

        long totalPresences = inscriptionRepository.countByPresentTrue();
        long totalInscrits = inscriptionRepository.count();
        double tauxParticipation = totalInscrits > 0 ? (totalPresences * 100.0) / totalInscrits : 0;
        stats.put("tauxParticipation", Math.round(tauxParticipation));

        return stats;
    }
}