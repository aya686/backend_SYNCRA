package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms2.entities.*;
import tn.esprit.ms2.repositories.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final SprintRepository sprintRepository;
    private final PlanningRepository planningRepository;
    private final TacheRepository tacheRepository;
    private final SousTacheRepository sousTacheRepository;
    private final JalonRepository jalonRepository;
    private final ObjectifRepository objectifRepository;
    private final AvancementRepository avancementRepository;

    public List<Projet> getAll() { return projetRepository.findAll(); }

    public Projet getById(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + id));
    }

    @Transactional
    public Projet create(Projet projet) {
        // 1. Statut initial
        if (projet.getStatut() == null) {
            projet.setStatut(StatutProjet.EN_ATTENTE);
        }

        // 2. Sauvegarder le projet
        Projet saved = projetRepository.save(projet);

        // 3. Générer les jalons
        genererJalons(saved);

        // 4. Générer les objectifs selon catégorie
        genererObjectifs(saved);

        // 5. Générer les sprints (1 par mois)
        List<Sprint> sprints = genererSprints(saved);

        // 6. Générer les tâches types selon catégorie
        genererTaches(saved, sprints);

        // 7. Initialiser l'avancement
        Avancement avancement = new Avancement();
        avancement.setProjet(saved);
        avancement.setPourcentage(0.0);
        avancement.setDateCalcul(LocalDate.now());
        avancement.setNotesSuivi("Projet initialisé automatiquement.");
        avancementRepository.save(avancement);

        return saved;
    }

    // ─────────────────────────────────────────
    // GÉNÉRATION JALONS
    // ─────────────────────────────────────────
    private void genererJalons(Projet projet) {
        LocalDate debut = projet.getDateDebut();
        LocalDate fin   = projet.getDateFin();
        if (debut == null || fin == null) return;

        long totalJours = ChronoUnit.DAYS.between(debut, fin);

        List<Jalon> jalons = new ArrayList<>();

        Jalon j1 = new Jalon();
        j1.setProjet(projet);
        j1.setTitre("Lancement du projet");
        j1.setDateEcheance(debut);
        j1.setAtteint(false);
        jalons.add(j1);

        Jalon j2 = new Jalon();
        j2.setProjet(projet);
        j2.setTitre("Mi-projet — Revue d'avancement");
        j2.setDateEcheance(debut.plusDays(totalJours / 2));
        j2.setAtteint(false);
        jalons.add(j2);

        Jalon j3 = new Jalon();
        j3.setProjet(projet);
        j3.setTitre("Livraison finale");
        j3.setDateEcheance(fin);
        j3.setAtteint(false);
        jalons.add(j3);

        jalonRepository.saveAll(jalons);
    }

    // ─────────────────────────────────────────
    // GÉNÉRATION OBJECTIFS
    // ─────────────────────────────────────────
    private void genererObjectifs(Projet projet) {
        List<String> titres = getObjectifsParCategorie(projet.getCategorie());
        List<Objectif> objectifs = new ArrayList<>();

        for (String titre : titres) {
            Objectif o = new Objectif();
            o.setProjet(projet);
            o.setTitre(titre);
            o.setDescription("Objectif généré automatiquement pour la catégorie " + projet.getCategorie());
            o.setAtteint(false);
            objectifs.add(o);
        }

        objectifRepository.saveAll(objectifs);
    }

    private List<String> getObjectifsParCategorie(String categorie) {
        if (categorie == null) categorie = "GENERAL";
        return switch (categorie.toUpperCase()) {
            case "TECH", "DEVELOPPEMENT" -> List.of(
                    "Définir l'architecture technique",
                    "Livrer la version MVP",
                    "Passer les tests de recette",
                    "Déployer en production"
            );
            case "ECOMMERCE", "COMMERCE" -> List.of(
                    "Créer le catalogue produits",
                    "Configurer les paiements",
                    "Lancer la boutique en ligne",
                    "Atteindre les premières ventes"
            );
            case "DESIGN", "CREATIF" -> List.of(
                    "Définir la charte graphique",
                    "Livrer les maquettes",
                    "Valider le prototype",
                    "Finaliser les livrables créatifs"
            );
            case "FORMATION", "EDUCATION" -> List.of(
                    "Créer le programme de formation",
                    "Développer les contenus",
                    "Piloter la première session",
                    "Évaluer et améliorer"
            );
            default -> List.of(
                    "Définir le périmètre du projet",
                    "Identifier les ressources nécessaires",
                    "Livrer le premier livrable",
                    "Clôturer et évaluer le projet"
            );
        };
    }

    // ─────────────────────────────────────────
    // GÉNÉRATION SPRINTS
    // ─────────────────────────────────────────
    private List<Sprint> genererSprints(Projet projet) {
        LocalDate debut = projet.getDateDebut();
        LocalDate fin   = projet.getDateFin();
        if (debut == null || fin == null) return List.of();

        List<Sprint> sprints = new ArrayList<>();
        LocalDate current = debut;
        int numero = 1;

        while (current.isBefore(fin)) {
            LocalDate finSprint = current.plusWeeks(2);
            if (finSprint.isAfter(fin)) finSprint = fin;

            Sprint sprint = new Sprint();
            sprint.setProjet(projet);
            sprint.setNom("Sprint " + numero);
            sprint.setDateDebut(current);
            sprint.setDateFin(finSprint);
            sprint.setStatut(StatutSprint.PLANIFIE);
            sprint.setChargeTotal(0.0);
            sprint = sprintRepository.save(sprint);

            // Planning associé
            Planning planning = new Planning();
            planning.setProjet(projet);
            planning.setSprint(sprint);
            planning.setCharge(0.0);
            planning.setCapacite(80.0); // 80h par sprint par défaut
            planning.setDateGeneration(LocalDate.now());
            planningRepository.save(planning);

            sprints.add(sprint);
            current = finSprint.plusDays(1);
            numero++;
        }

        return sprints;
    }

    // ─────────────────────────────────────────
    // GÉNÉRATION TÂCHES
    // ─────────────────────────────────────────
    private void genererTaches(Projet projet, List<Sprint> sprints) {
        List<TacheTemplate> templates = getTachesParCategorie(projet.getCategorie());
        List<Tache> taches = new ArrayList<>();

        int sprintIndex = 0;
        for (int i = 0; i < templates.size(); i++) {
            TacheTemplate tmpl = templates.get(i);

            Sprint sprintAssocie = sprints.isEmpty() ? null
                    : sprints.get(Math.min(sprintIndex, sprints.size() - 1));

            Tache tache = new Tache();
            tache.setProjet(projet);
            tache.setTitre(tmpl.titre());
            tache.setDescription(tmpl.description());
            tache.setPriorite(tmpl.priorite());
            tache.setStatut(StatutTache.A_FAIRE);
            tache.setEstimationHeures(tmpl.estimationH());

            // Deadline = fin du sprint associé
            if (sprintAssocie != null) {
                tache.setDeadline(sprintAssocie.getDateFin());
            }

            Tache savedTache = tacheRepository.save(tache);

            // Sous-tâches
            for (String sousTitre : tmpl.sousTaches()) {
                SousTache st = new SousTache();
                st.setTache(savedTache);
                st.setTitre(sousTitre);
                st.setStatut(StatutSousTache.A_FAIRE);                  sousTacheRepository.save(st);
            }

            taches.add(savedTache);

            // Changer de sprint tous les 2-3 tâches
            if ((i + 1) % 3 == 0) sprintIndex++;
        }
    }

    private record TacheTemplate(
            String titre,
            String description,
            PrioriteTache priorite,
            double estimationH,
            List<String> sousTaches
    ) {}

    private List<TacheTemplate> getTachesParCategorie(String categorie) {
        if (categorie == null) categorie = "GENERAL";
        return switch (categorie.toUpperCase()) {
            case "TECH", "DEVELOPPEMENT" -> List.of(
                    new TacheTemplate("Analyse des besoins", "Recueillir et documenter les besoins fonctionnels",
                            PrioriteTache.HAUTE, 8, List.of("Interviews parties prenantes", "Rédiger user stories", "Valider le périmètre")),
                    new TacheTemplate("Conception de l'architecture", "Définir l'architecture technique et les choix technologiques",
                            PrioriteTache.HAUTE, 12, List.of("Diagramme de composants", "Choix de la stack", "Revue d'architecture")),
                    new TacheTemplate("Développement Backend", "Implémenter les APIs et la logique métier",
                            PrioriteTache.HAUTE, 24, List.of("Entités et repositories", "Services métier", "Controllers REST", "Tests unitaires")),
                    new TacheTemplate("Développement Frontend", "Implémenter les interfaces utilisateur",
                            PrioriteTache.HAUTE, 20, List.of("Structure de l'app", "Pages principales", "Intégration API", "Responsive design")),
                    new TacheTemplate("Tests et validation", "Tests fonctionnels et correction des bugs",
                            PrioriteTache.MOYENNE, 10, List.of("Tests fonctionnels", "Tests de performance", "Corrections")),
                    new TacheTemplate("Déploiement", "Mise en production de l'application",
                            PrioriteTache.HAUTE, 6, List.of("Préparation serveur", "CI/CD", "Monitoring"))
            );
            case "ECOMMERCE", "COMMERCE" -> List.of(
                    new TacheTemplate("Catalogue produits", "Créer et structurer le catalogue",
                            PrioriteTache.HAUTE, 10, List.of("Catégories", "Fiche produit", "Photos")),
                    new TacheTemplate("Configuration paiements", "Intégrer les moyens de paiement",
                            PrioriteTache.HAUTE, 8, List.of("Passerelle paiement", "Tests transactions")),
                    new TacheTemplate("Configuration livraisons", "Paramétrer les modes de livraison",
                            PrioriteTache.MOYENNE, 6, List.of("Transporteurs", "Zones", "Tarifs")),
                    new TacheTemplate("Marketing de lancement", "Préparer le lancement commercial",
                            PrioriteTache.MOYENNE, 8, List.of("Réseaux sociaux", "Newsletter", "SEO"))
            );
            case "DESIGN", "CREATIF" -> List.of(
                    new TacheTemplate("Brief créatif", "Définir la direction artistique",
                            PrioriteTache.HAUTE, 6, List.of("Moodboard", "Charte graphique", "Validation client")),
                    new TacheTemplate("Maquettes", "Créer les maquettes et wireframes",
                            PrioriteTache.HAUTE, 16, List.of("Wireframes", "Maquettes HD", "Prototype interactif")),
                    new TacheTemplate("Production", "Réaliser les livrables finaux",
                            PrioriteTache.HAUTE, 20, List.of("Déclinaisons", "Export fichiers", "Livraison"))
            );
            default -> List.of(
                    new TacheTemplate("Cadrage du projet", "Définir le périmètre et les objectifs",
                            PrioriteTache.HAUTE, 8, List.of("Réunion de lancement", "Définir les rôles", "Planning initial")),
                    new TacheTemplate("Planification détaillée", "Établir le plan de travail complet",
                            PrioriteTache.HAUTE, 6, List.of("WBS", "Estimation charges", "Affectations")),
                    new TacheTemplate("Exécution phase 1", "Réaliser les premiers livrables",
                            PrioriteTache.HAUTE, 20, List.of("Livrable 1", "Livrable 2", "Revue intermédiaire")),
                    new TacheTemplate("Exécution phase 2", "Finaliser les livrables",
                            PrioriteTache.MOYENNE, 20, List.of("Livrable 3", "Tests", "Corrections")),
                    new TacheTemplate("Clôture", "Clôturer le projet et livrer",
                            PrioriteTache.MOYENNE, 6, List.of("Bilan projet", "Documentation", "Réception"))
            );
        };
    }

    // ─────────────────────────────────────────
    // AUTRES MÉTHODES
    // ─────────────────────────────────────────
    public Projet update(Long id, Projet updated) {
        Projet p = getById(id);
        p.setTitre(updated.getTitre());
        p.setDescription(updated.getDescription());
        p.setStatut(updated.getStatut());
        p.setDateDebut(updated.getDateDebut());
        p.setDateFin(updated.getDateFin());
        p.setBudget(updated.getBudget());
        p.setCategorie(updated.getCategorie());
        p.setPorteurId(updated.getPorteurId());
        p.setModeGuidage(updated.getModeGuidage()); // ✅
        p.setMoniteurId(updated.getMoniteurId());   // ✅
        return projetRepository.save(p);
    }

    public void delete(Long id) { projetRepository.deleteById(id); }

    @Transactional(readOnly = true)
    public List<Projet> getByPorteur(Long porteurId) {
        return projetRepository.findByPorteurId(porteurId);
    }
    public List<Projet> getByStatut(StatutProjet statut) { return projetRepository.findByStatut(statut); }

    // Recalculer l'avancement d'un projet selon les tâches terminées
    @Transactional
    public Avancement recalculerAvancement(Long projetId) {
        Projet projet = getById(projetId);
        List<Tache> taches = tacheRepository.findByProjetId(projetId);

        int total = taches.size();
        long terminees = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINE)
                .count();

        double pourcentage = total == 0 ? 0.0 : (terminees * 100.0) / total;

        Avancement avancement = avancementRepository.findByProjetId(projetId)
                .orElse(new Avancement());
        avancement.setProjet(projet);
        avancement.setPourcentage(pourcentage);
        avancement.setDateCalcul(LocalDate.now());
        avancement.setNotesSuivi(terminees + " tâche(s) terminée(s) sur " + total);

        return avancementRepository.save(avancement);
    }
    public Avancement getAvancement(Long projetId) {
        return avancementRepository.findByProjetId(projetId)
                .orElseGet(() -> {
                    Avancement a = new Avancement();
                    a.setPourcentage(0.0);
                    return a;
                });
    }
    public Projet assignerMoniteur(Long projetId, Long moniteurId) {
        Projet p = getById(projetId);
        p.setMoniteurId(moniteurId);
        p.setModeGuidage(ModeGuidage.MONITEUR);
        return projetRepository.save(p);
    }

    public List<Projet> getProjetsParMoniteur(Long moniteurId) {
        return projetRepository.findByMoniteurId(moniteurId);
    }
}