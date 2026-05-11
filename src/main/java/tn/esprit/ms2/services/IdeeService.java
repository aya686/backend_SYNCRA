package tn.esprit.ms2.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ms2.entities.Idee;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.entities.StatutIdee;
import tn.esprit.ms2.repositories.IdeeRepository;
import tn.esprit.ms2.DTO.TransformationResultDTO;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdeeService {

    private final IdeeRepository ideeRepository;
    private final ProjetService projetService;

    public List<Idee> getAll() { return ideeRepository.findAll(); }

    public Idee getById(Long id) {
        return ideeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Idée non trouvée: " + id));
    }

    public Idee create(Idee idee) {
        if (idee.getTitre() == null || idee.getTitre().trim().length() < 3)
            throw new RuntimeException("Le titre doit avoir au moins 3 caractères.");
        if (idee.getCategorie() == null || idee.getCategorie().isBlank())
            throw new RuntimeException("La catégorie est obligatoire.");
        if (idee.getAuteurId() == null)
            throw new RuntimeException("L'auteur est obligatoire.");

        idee.setStatut(StatutIdee.NOUVELLE);
        idee.setProjet(null);
        idee.setTitre(idee.getTitre().trim());
        return ideeRepository.save(idee);
    }

    public Idee update(Long id, Idee updated) {
        Idee i = getById(id);
        if (i.getStatut() == StatutIdee.TRANSFORMEE)
            throw new RuntimeException("Impossible de modifier une idée déjà transformée.");

        if (updated.getTitre() != null && updated.getTitre().trim().length() >= 3)
            i.setTitre(updated.getTitre().trim());
        if (updated.getDescription() != null)
            i.setDescription(updated.getDescription());
        if (updated.getCategorie() != null)
            i.setCategorie(updated.getCategorie());
        if (updated.getStatut() != null && updated.getStatut() != StatutIdee.TRANSFORMEE)
            i.setStatut(updated.getStatut());

        return ideeRepository.save(i);
    }

    public void delete(Long id) { ideeRepository.deleteById(id); }

    public TransformationResultDTO transformerEnProjet(Long ideeId, Projet projetData) {
        Idee idee = getById(ideeId);
        if (idee.getStatut() == StatutIdee.TRANSFORMEE)
            throw new RuntimeException("Déjà transformée.");

        projetData.setCategorie(idee.getCategorie());
        Projet projet = projetService.create(projetData);
        idee.setProjet(projet);
        idee.setStatut(StatutIdee.TRANSFORMEE);
        Idee savedIdee = ideeRepository.save(idee);
        return new TransformationResultDTO(savedIdee, projet);
    }

    public List<Idee> getSansProjet() { return ideeRepository.findByProjetIsNull(); }
    public List<Idee> getByAuteur(Long auteurId) { return ideeRepository.findByAuteurId(auteurId); }
}