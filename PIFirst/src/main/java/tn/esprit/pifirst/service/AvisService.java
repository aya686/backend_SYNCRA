package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Avis;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.AvisRepository;
import tn.esprit.pifirst.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvisService {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Avis> getAll() {
        return avisRepository.findAll();
    }

    public Avis getById(Long id) {
        return avisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
    }

    public List<Avis> getByCible(Long idCible) {
        return avisRepository.findByCibleId(idCible);
    }

    public Double getScoreMoyen(Long idCible) {
        return avisRepository.findAvgNoteByCibleId(idCible);
    }

    public Avis create(Long idAuteur, Long idCible, Avis avis) {
        User auteur = userRepository.findById(idAuteur)
                .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));
        User cible = userRepository.findById(idCible)
                .orElseThrow(() -> new RuntimeException("Cible non trouvée"));

        avis.setAuteur(auteur);
        avis.setCible(cible);
        avis.setDate(LocalDateTime.now());

        return avisRepository.save(avis);
    }

    public Avis repondre(Long id, String reponse) {
        Avis avis = getById(id);
        avis.setReponse(reponse);
        return avisRepository.save(avis);
    }

    public void delete(Long id) {
        avisRepository.deleteById(id);
    }
}