// FreelancerService.java
package tn.esprit.pifirst.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pifirst.entity.Freelancer;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.FreelancerRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.util.List;

@Service
public class FreelancerService {

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Freelancer> getAll() {
        return freelancerRepository.findAll();
    }

    public Freelancer getById(Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Freelancer non trouvé"));
    }

    public List<Freelancer> getDisponibles() {
        return freelancerRepository.findByDisponibleTrue();
    }

    @Transactional
    public Freelancer create(Freelancer freelancer, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        entityManager.createNativeQuery(
                        "INSERT INTO freelancers (id_user, description, cv, disponible) VALUES (?, ?, ?, ?)")
                .setParameter(1, user.getId())
                .setParameter(2, freelancer.getDescription())
                .setParameter(3, freelancer.getCv())
                .setParameter(4, freelancer.getDisponible())
                .executeUpdate();

        freelancer.setId(user.getId());
        freelancer.setEmail(user.getEmail());
        freelancer.setNom(user.getNom());
        freelancer.setPrenom(user.getPrenom());
        return freelancer;
    }

    public Freelancer update(Long id, Freelancer updated) {
        Freelancer existing = getById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setPhoto(updated.getPhoto());
        existing.setDescription(updated.getDescription());
        existing.setCv(updated.getCv());
        return freelancerRepository.save(existing);
    }

    public void delete(Long id) {
        freelancerRepository.deleteById(id);
    }
}