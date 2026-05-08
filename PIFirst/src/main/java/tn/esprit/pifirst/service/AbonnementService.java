package tn.esprit.pifirst.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.*;
import tn.esprit.pifirst.enums.StatutAbonnement;
import tn.esprit.pifirst.enums.StatutCodePromo;
import tn.esprit.pifirst.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final CodePromoRepository codePromoRepository;

    public List<Abonnement> getAll() {
        return abonnementRepository.findAll();
    }

    public Abonnement getById(Long id) {
        return abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));
    }

    public List<Abonnement> getByUser(Long idUser) {
        return abonnementRepository.findByUserId(idUser);
    }

    public Abonnement getAbonnementActif(Long idUser) {
        return abonnementRepository
                .findByUserIdAndStatut(idUser, StatutAbonnement.ACTIF)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif"));
    }

    // SOUSCRIRE AVEC CODE PROMO (fonctionnalité avancée)
    public Abonnement souscrireAvecCodePromo(Long idUser, Long idPlan, String codePromo) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        Plan plan = planRepository.findById(idPlan)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        CodePromo code = null;
        if (codePromo != null && !codePromo.isEmpty()) {
            code = validerCodePromo(codePromo, idUser);
        }

        Abonnement abonnement = new Abonnement();
        abonnement.setUser(user);
        abonnement.setPlan(plan);
        abonnement.setDateDebut(LocalDateTime.now());
        abonnement.setDateFin(LocalDateTime.now().plusDays(plan.getDureeJours()));
        abonnement.setStatut(StatutAbonnement.ACTIF);
        abonnement.setRenouvellementAuto(false);
        abonnement.setDerniereActivite(LocalDateTime.now());
        abonnement.setCodePromo(code);

        // Incrémenter utilisation du code
        if (code != null) {
            code.setNbUtilisationsCourant(code.getNbUtilisationsCourant() + 1);
            if (code.getNbUtilisationsMax() != null &&
                    code.getNbUtilisationsCourant() >= code.getNbUtilisationsMax()) {
                code.setStatut(StatutCodePromo.EPUISE);
            }
            codePromoRepository.save(code);
        }

        return abonnementRepository.save(abonnement);
    }

    // Méthode de validation du code promo (logique métier centralisée)
    private CodePromo validerCodePromo(String code, Long idUser) {
        CodePromo codePromo = codePromoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Code promo invalide"));

        // Vérifier statut
        if (codePromo.getStatut() != StatutCodePromo.ACTIF) {
            throw new RuntimeException("Code promo " + codePromo.getStatut().toString().toLowerCase());
        }

        // Vérifier expiration
        if (codePromo.getDateExpiration() != null &&
                codePromo.getDateExpiration().isBefore(LocalDateTime.now())) {
            codePromo.setStatut(StatutCodePromo.EXPIRE);
            codePromoRepository.save(codePromo);
            throw new RuntimeException("Code promo expiré");
        }

        // Vérifier utilisation max
        if (codePromo.getNbUtilisationsMax() != null &&
                codePromo.getNbUtilisationsCourant() >= codePromo.getNbUtilisationsMax()) {
            codePromo.setStatut(StatutCodePromo.EPUISE);
            codePromoRepository.save(codePromo);
            throw new RuntimeException("Code promo épuisé");
        }

        // Vérifier usage unique par user
        if (abonnementRepository.existsByUserIdAndCodePromoId(idUser, codePromo.getId())) {
            throw new RuntimeException("Vous avez déjà utilisé ce code promo");
        }

        return codePromo;
    }

    public Abonnement souscrire(Long idUser, Long idPlan) {
        return souscrireAvecCodePromo(idUser, idPlan, null);
    }

    // Mettre à jour dernière activité
    public void updateDerniereActivite(Long idAbonnement) {
        Abonnement abonnement = getById(idAbonnement);
        abonnement.setDerniereActivite(LocalDateTime.now());
        abonnementRepository.save(abonnement);
    }

    // Suggestion downgrade (fonctionnalité avancée)
    public boolean devraitDowngrade(Long idAbonnement, int joursInactivite) {
        Abonnement abonnement = getById(idAbonnement);
        if (abonnement.getDerniereActivite() == null) {
            return false;
        }
        LocalDateTime seuil = LocalDateTime.now().minusDays(joursInactivite);
        return abonnement.getDerniereActivite().isBefore(seuil);
    }

    public Abonnement resilier(Long id) {
        Abonnement abonnement = getById(id);
        abonnement.setStatut(StatutAbonnement.RESILIE);
        return abonnementRepository.save(abonnement);
    }

    public void delete(Long id) {
        abonnementRepository.deleteById(id);
    }

    public Abonnement update(Long id, Abonnement updated) {
        Abonnement existing = getById(id);
        existing.setRenouvellementAuto(updated.getRenouvellementAuto());
        return abonnementRepository.save(existing);
    }
}