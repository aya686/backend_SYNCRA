package tn.esprit.pifirst.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pifirst.entity.Etudiant;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.EtudiantRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EtudiantService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Etudiant> getAll() {
        return etudiantRepository.findAll();
    }

    public Etudiant getById(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etudiant non trouvé"));
    }

    public List<Etudiant> getAccesExpires() {
        return etudiantRepository.findByDateFinAccesGratuitBefore(LocalDateTime.now());
    }

    @Transactional
    public Etudiant create(Etudiant etudiant, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé: " + idUser));

        // Copier les données du user dans l'objet etudiant
        etudiant.setId(user.getId());
        etudiant.setEmail(user.getEmail());
        etudiant.setPassword(user.getPassword());
        etudiant.setNom(user.getNom());
        etudiant.setPrenom(user.getPrenom());
        etudiant.setPhoto(user.getPhoto());
        etudiant.setTelephone(user.getTelephone());
        etudiant.setStatut(user.getStatut());
        etudiant.setDateInscription(user.getDateInscription());
        etudiant.setDerniereConnexion(user.getDerniereConnexion());
        etudiant.setDateFinAccesGratuit(LocalDateTime.now().plusDays(30));

        // INSERT direct uniquement dans la table etudiants
        entityManager.createNativeQuery(
                        "INSERT INTO etudiants (id_user, filiere, universite, annee_etude, email_universitaire, date_fin_acces_gratuit) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, user.getId())
                .setParameter(2, etudiant.getFiliere())
                .setParameter(3, etudiant.getUniversite())
                .setParameter(4, etudiant.getAnneeEtude())
                .setParameter(5, etudiant.getEmailUniversitaire())
                .setParameter(6, etudiant.getDateFinAccesGratuit())
                .executeUpdate();

        return etudiant;
    }

    public Etudiant update(Long id, Etudiant updated) {
        Etudiant existing = getById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setPhoto(updated.getPhoto());
        existing.setUniversite(updated.getUniversite());
        existing.setFiliere(updated.getFiliere());
        existing.setAnneeEtude(updated.getAnneeEtude());
        existing.setEmailUniversitaire(updated.getEmailUniversitaire());
        return etudiantRepository.save(existing);
    }

    public void delete(Long id) {
        etudiantRepository.deleteById(id);
    }
}