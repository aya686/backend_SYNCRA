package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;


import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.enums.Statut;
import tn.esprit.pifirst.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
    }

    public User create(User user) {
        System.out.println("=== CRÉATION USER ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Nom: " + user.getNom());
        System.out.println("Prénom: " + user.getPrenom());

        user.setDateInscription(LocalDateTime.now());
        user.setStatut(Statut.ACTIF);

        // 🔐 AJOUTE CETTE LIGNE : Hasher le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            User saved = userRepository.save(user);
            System.out.println("✅ User créé avec ID: " + saved.getId());
            return saved;
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public User update(Long id, User updated) {
        User existing = getById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setPhoto(updated.getPhoto());
        existing.setTelephone(updated.getTelephone());
        existing.setStatut(updated.getStatut());

        // Si un nouveau mot de passe est fourni, le hasher
        if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        return userRepository.save(existing);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}