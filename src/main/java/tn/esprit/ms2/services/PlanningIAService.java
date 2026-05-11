package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.*;
import tn.esprit.ms2.repositories.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningIAService {

    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final AlerteRepository alerteRepository;
    private final SessionTravailRepository sessionTravailRepository;

    // Tourne toutes les heures
    @Scheduled(fixedRate = 3600000)
    public void analyserTousLesProjets() {
        List<Projet> projets = projetRepository.findAll();
        projets.forEach(this::analyserProjet);
    }

    public void analyserProjet(Projet projet) {
        List<Tache> taches = tacheRepository.findByProjetId(projet.getId());
        if (taches.isEmpty()) return;

        long total     = taches.size();
        long terminees = taches.stream().filter(t -> t.getStatut() == StatutTache.TERMINE).count();
        long bloquees  = taches.stream().filter(t -> t.getStatut() == StatutTache.BLOQUE).count();
        long enRetard  = taches.stream().filter(t ->
                t.getDeadline() != null &&
                        t.getDeadline().isBefore(LocalDate.now()) &&
                        t.getStatut() != StatutTache.TERMINE
        ).count();

        double tauxAvancement = (double) terminees / total * 100;

        // Alerte retard
        if (enRetard > 0) {
            creerAlertesSiAbsente(projet, TypeAlerte.DEADLINE,
                    enRetard + " tâche(s) en retard sur le projet : " + projet.getTitre());
        }

        // Alerte blocage
        if (bloquees > 1) {
            creerAlertesSiAbsente(projet, TypeAlerte.BLOCAGE,
                    bloquees + " tâche(s) bloquées détectées.");
        }

        // Alerte surcharge — sessions récentes > 8h/jour
        List<SessionTravail> sessionsDuJour = sessionTravailRepository
                .findByProjetIdAndDateAfter(projet.getId(), LocalDate.now().minusDays(1));
        double totalHeures = sessionsDuJour.stream()
                .mapToDouble(s -> s.getDureeMinutes() != null ? s.getDureeMinutes() / 60.0 : 0)                .sum();
        if (totalHeures > 8) {
            creerAlertesSiAbsente(projet, TypeAlerte.SURCHARGE,
                    "Surcharge détectée : " + String.format("%.1f", totalHeures) + "h travaillées aujourd'hui.");
        }

        // Alerte stagnation — avancement < 20% et plus de 30% du temps écoulé
        if (projet.getDateDebut() != null && projet.getDateFin() != null) {
            long totalJours  = ChronoUnit.DAYS.between(projet.getDateDebut(), projet.getDateFin());
            long joursEcoules = ChronoUnit.DAYS.between(projet.getDateDebut(), LocalDate.now());
            if (totalJours > 0) {
                double pctTemps = (double) joursEcoules / totalJours * 100;
                if (pctTemps > 30 && tauxAvancement < 20) {
                    creerAlertesSiAbsente(projet, TypeAlerte.STAGNATION,
                            "Stagnation détectée : seulement " +
                                    String.format("%.0f", tauxAvancement) + "% d'avancement alors que " +
                                    String.format("%.0f", pctTemps) + "% du temps est écoulé.");
                }
            }
        }
    }

    private void creerAlertesSiAbsente(Projet projet, TypeAlerte type, String message) {
        boolean existe = alerteRepository.existsByProjetIdAndTypeAndTraitee(
                projet.getId(), type, false);
        if (!existe) {
            Alerte alerte = new Alerte();
            alerte.setProjet(projet);
            alerte.setUtilisateurId(projet.getPorteurId());
            alerte.setType(type);
            alerte.setMessage(message);
            alerte.setDate(LocalDate.now());
            alerte.setTraitee(false);
            alerteRepository.save(alerte);
        }
    }
}