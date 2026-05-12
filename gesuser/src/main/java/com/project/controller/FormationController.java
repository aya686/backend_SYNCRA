// FormationController.java - VERSION CORRIGÉE
package com.project.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.entity.Formation;
import com.project.entity.Formateur;
import com.project.dto.FormationRequest;
import com.project.service.FormationService;
import com.project.service.FormateurService;

@RestController
@RequestMapping("/api/formations")
public class FormationController {

    @Autowired
    private FormationService service;

    @Autowired
    private FormateurService formateurService;

    @GetMapping
    public List<Formation> getAll() {
        return service.getAll();
    }

    @GetMapping("/formateurs")
    public List<Formateur> getAllFormateurs() {
        return formateurService.getAll();
    }

    // ✅ CRÉATION - UTILISER FormationRequest
    // FormationController.java - Vérifiez que cette méthode existe
    @PostMapping
    public Formation create(@RequestBody FormationRequest request) {
        System.out.println("=== CRÉATION FORMATION ===");
        System.out.println("Titre: " + request.getTitre());
        System.out.println("formateurId reçu: " + request.getFormateurId());  // ← VITAL

        Formation formation = new Formation();
        formation.setTitre(request.getTitre());
        formation.setDescription(request.getDescription());
        formation.setDureeHeures(request.getDureeHeures());
        formation.setNiveau(request.getNiveau());
        formation.setPrix(request.getPrix());
        formation.setCertificate(request.getCertificate());
        formation.setStatut(request.getStatut() != null ? request.getStatut() : "active");

        if (request.getFormateurId() != null) {
            Formateur formateur = formateurService.getById(request.getFormateurId());
            formation.setFormateur(formateur);
            System.out.println("✅ Formateur assigné: " + formateur.getNom());
        } else {
            System.out.println("⚠️ formateurId est NULL");
        }

        return service.save(formation);
    }

    @GetMapping("/{id}")
    public Formation getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ✅ MODIFICATION - UTILISER FormationRequest aussi
    @PutMapping("/{id}")
    public Formation update(@PathVariable Long id, @RequestBody FormationRequest request) {
        Formation formation = service.getById(id);
        formation.setTitre(request.getTitre());
        formation.setDescription(request.getDescription());
        formation.setDureeHeures(request.getDureeHeures());
        formation.setNiveau(request.getNiveau());
        formation.setPrix(request.getPrix());
        formation.setCertificate(request.getCertificate());
        formation.setStatut(request.getStatut() != null ? request.getStatut() : formation.getStatut());

        if (request.getFormateurId() != null && request.getFormateurId() > 0) {
            Formateur formateur = formateurService.getById(request.getFormateurId());
            formation.setFormateur(formateur);
        } else {
            formation.setFormateur(null);
        }

        return service.save(formation);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Formation supprimée avec succès !";
    }
}