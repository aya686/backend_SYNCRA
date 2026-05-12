package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Competence;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.CompetenceRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.util.List;

@Service
public class CompetenceService {

    @Autowired
    private CompetenceRepository competenceRepository;

    @Autowired
    private UserRepository userRepository;  // ← AJOUTER CETTE LIGNE

    public List<Competence> getAll() {
        return competenceRepository.findAll();
    }

    public Competence getById(Long id) {
        return competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée"));
    }

    // AJOUTER CETTE MÉTHODE
    public List<Competence> getByUser(Long idUser) {
        return competenceRepository.findByUserId(idUser);
    }

    // AJOUTER CETTE MÉTHODE
    public Competence create(Competence competence, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        competence.setUser(user);
        return competenceRepository.save(competence);
    }

    public Competence update(Long id, Competence updated) {
        Competence existing = getById(id);
        existing.setLibelle(updated.getLibelle());
        existing.setNiveau(updated.getNiveau());
        existing.setCategorie(updated.getCategorie());
        return competenceRepository.save(existing);
    }

    public void delete(Long id) {
        competenceRepository.deleteById(id);
    }
}