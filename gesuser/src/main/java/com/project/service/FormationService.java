package com.project.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.entity.Formation;
import com.project.entity.Formateur;
import com.project.repository.FormationRepository;
import com.project.repository.FormateurRepository;

// FormationService.java
@Service
public class FormationService {

    @Autowired
    private FormationRepository repo;

    @Autowired
    private FormateurRepository formateurRepo;

    public List<Formation> getAll() {
        return repo.findAll();
    }

    public Formation save(Formation formation) {
        // Le formateur est déjà assigné dans le contrôleur
        return repo.save(formation);
    }

    public Formation getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec id: " + id));
    }

    public Formation update(Long id, Formation formationDetails) {
        Formation formation = getById(id);
        formation.setTitre(formationDetails.getTitre());
        formation.setDescription(formationDetails.getDescription());
        formation.setNiveau(formationDetails.getNiveau());
        formation.setDureeHeures(formationDetails.getDureeHeures());
        formation.setCertificate(formationDetails.getCertificate());
        formation.setPrix(formationDetails.getPrix());
        formation.setStatut(formationDetails.getStatut());
        formation.setFormateur(formationDetails.getFormateur());

        return repo.save(formation);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}