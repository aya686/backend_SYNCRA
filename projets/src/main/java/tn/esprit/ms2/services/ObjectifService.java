package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Objectif;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.repositories.ObjectifRepository;
import tn.esprit.ms2.repositories.ProjetRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObjectifService {

    private final ObjectifRepository objectifRepository;
    private final ProjetRepository projetRepository;

    public List<Objectif> getAll() { return objectifRepository.findAll(); }

    public Objectif getById(Long id) {
        return objectifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objectif non trouvé: " + id));
    }

    public Objectif create(Long projetId, Objectif objectif) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetId));
        objectif.setProjet(projet);
        return objectifRepository.save(objectif);
    }

    public Objectif update(Long id, Objectif updated) {
        Objectif o = getById(id);
        o.setTitre(updated.getTitre());
        o.setDescription(updated.getDescription());
        o.setAtteint(updated.getAtteint());
        return objectifRepository.save(o);
    }

    public void delete(Long id) { objectifRepository.deleteById(id); }
    public List<Objectif> getByProjet(Long projetId) { return objectifRepository.findByProjetId(projetId); }
}