package com.example.projetpi.controller;

import com.example.projetpi.entity.AlerteBurnout;
import com.example.projetpi.entity.Consultation;
import com.example.projetpi.entity.DossierSante;
import com.example.projetpi.entity.ProgrammePrevention;
import com.example.projetpi.repository.AlerteBurnoutRepository;
import com.example.projetpi.repository.DossierSanteRepository;
import com.example.projetpi.repository.ProgrammePreventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private AlerteBurnoutRepository alerteBurnoutRepository;

    @Autowired
    private DossierSanteRepository dossierSanteRepository;

    @Autowired
    private ProgrammePreventionRepository programmePreventionRepository;

    // ── GET: Alertes par utilisateur ──────────────────────
    @GetMapping("/alertes-burnout/utilisateur/{userId}")
    public ResponseEntity<List<AlerteBurnout>> getAlertesByUtilisateur(@PathVariable Long userId) {
        List<AlerteBurnout> alertes = alerteBurnoutRepository.findByUtilisateurId(userId);
        // Trier par date décroissante
        alertes.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return ResponseEntity.ok(alertes);
    }



    // ── GET: Programmes par utilisateur ───────────────────
    @GetMapping("/programmes-prevention/utilisateur/{userId}")
    public ResponseEntity<List<ProgrammePrevention>> getProgrammesByUtilisateur(@PathVariable Long userId) {
        List<ProgrammePrevention> programmes = programmePreventionRepository.findByUtilisateurId(userId);
        return ResponseEntity.ok(programmes);
    }

    // ── GET: Dashboard summary (endpoint optimisé) ────────
    @GetMapping("/dashboard/utilisateur/{userId}")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(@PathVariable Long userId) {
        Map<String, Object> summary = new HashMap<>();

        // Alertes
        List<AlerteBurnout> alertes = alerteBurnoutRepository.findByUtilisateurId(userId);
        alertes.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        long nonTraitees = alertes.stream().filter(a -> !Boolean.TRUE.equals(a.getTraitee())).count();
        AlerteBurnout derniere = alertes.isEmpty() ? null : alertes.get(0);

        summary.put("activeAlerts", nonTraitees);
        summary.put("lastAlert", derniere);
        summary.put("recentAlerts", alertes.stream().limit(5).collect(Collectors.toList()));
        summary.put("totalAlerts", alertes.size());

        // Score burnout calculé
        int burnoutScore = computeBurnoutScore(alertes);
        summary.put("burnoutScore", burnoutScore);
        summary.put("riskLevel", getRiskLevel(burnoutScore));

        // Dossier + prochaine consultation
        Optional<DossierSante> dossierOpt = dossierSanteRepository.findByUtilisateurId(userId);
        if (dossierOpt.isPresent()) {
            DossierSante dossier = dossierOpt.get();
            Optional<Consultation> nextConsultation = dossier.getConsultations().stream()
                    .filter(c -> c.getDate() != null && c.getDate().isAfter(LocalDate.now().minusDays(1)))
                    .min(Comparator.comparing(Consultation::getDate));
            summary.put("nextConsultation", nextConsultation.orElse(null));
        }

        // Programme actif
        Optional<ProgrammePrevention> programmeActif = programmePreventionRepository
                .findByUtilisateurId(userId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActif()))
                .findFirst();
        summary.put("activeProgramme", programmeActif.orElse(null));

        return ResponseEntity.ok(summary);
    }

    // ── POST: Marquer alerte comme traitée ─────────────────
    @PutMapping("/alertes-burnout/{id}/traiter")
    public ResponseEntity<AlerteBurnout> traiterAlerte(@PathVariable Long id) {
        Optional<AlerteBurnout> alerteOpt = alerteBurnoutRepository.findById(id);
        if (alerteOpt.isEmpty()) return ResponseEntity.notFound().build();

        AlerteBurnout alerte = alerteOpt.get();
        alerte.setTraitee(true);
        alerteBurnoutRepository.save(alerte);
        return ResponseEntity.ok(alerte);
    }

    // ── PRIVATE: Calcul score burnout ─────────────────────
    private int computeBurnoutScore(List<AlerteBurnout> alertes) {
        if (alertes.isEmpty()) return 1;

        List<AlerteBurnout> recent = alertes.stream().limit(7).collect(Collectors.toList());
        Map<String, Integer> scores = Map.of(
                "FAIBLE", 2, "MODÉRÉ", 5, "MODERE", 5, "ÉLEVÉ", 7, "ELEVE", 7, "CRITIQUE", 9
        );

        int total = recent.stream()
                .mapToInt(a -> scores.getOrDefault(
                        a.getNiveauRisque() != null ? a.getNiveauRisque().toUpperCase() : "", 3
                ))
                .sum();

        return Math.min(10, Math.round((float) total / recent.size()));
    }

    private String getRiskLevel(int score) {
        if (score <= 3) return "Faible";
        if (score <= 6) return "Modéré";
        return "Critique";
    }
}