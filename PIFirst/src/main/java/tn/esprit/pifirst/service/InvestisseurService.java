// InvestisseurService.java
package tn.esprit.pifirst.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pifirst.entity.Investisseur;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.InvestisseurRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.util.List;

@Service
public class InvestisseurService {

    @Autowired
    private InvestisseurRepository investisseurRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Investisseur> getAll() {
        return investisseurRepository.findAll();
    }

    public Investisseur getById(Long id) {
        return investisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investisseur non trouvé"));
    }

    public List<Investisseur> getParBudgetMin(Double montant) {
        return investisseurRepository.findByBudgetMaxGreaterThanEqual(montant);
    }

    @Transactional
    public Investisseur create(Investisseur investisseur, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        entityManager.createNativeQuery(
                        "INSERT INTO investisseurs (id_user, description, domaines_interet, budget_min, budget_max, nb_projets_finances) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, user.getId())
                .setParameter(2, investisseur.getDescription())
                .setParameter(3, investisseur.getDomainesInteret())
                .setParameter(4, investisseur.getBudgetMin())
                .setParameter(5, investisseur.getBudgetMax())
                .setParameter(6, investisseur.getNbProjetsFinances())
                .executeUpdate();

        investisseur.setId(user.getId());
        investisseur.setEmail(user.getEmail());
        investisseur.setNom(user.getNom());
        investisseur.setPrenom(user.getPrenom());
        return investisseur;
    }

    public Investisseur update(Long id, Investisseur updated) {
        Investisseur existing = getById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setPhoto(updated.getPhoto());
        existing.setDomainesInteret(updated.getDomainesInteret());
        existing.setBudgetMin(updated.getBudgetMin());
        existing.setBudgetMax(updated.getBudgetMax());
        existing.setNbProjetsFinances(updated.getNbProjetsFinances());
        existing.setDescription(updated.getDescription());
        return investisseurRepository.save(existing);
    }

    public void delete(Long id) {
        investisseurRepository.deleteById(id);
    }
}