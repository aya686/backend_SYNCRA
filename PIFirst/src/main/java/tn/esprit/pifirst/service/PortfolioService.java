package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Portfolio;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.repository.PortfolioRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.util.List;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;  // ← AJOUTER CETTE LIGNE

    public List<Portfolio> getAll() {
        return portfolioRepository.findAll();
    }

    public Portfolio getById(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio non trouvé"));
    }

    // AJOUTER CETTE MÉTHODE
    public List<Portfolio> getByUser(Long idUser) {
        return portfolioRepository.findByUserId(idUser);
    }

    // AJOUTER CETTE MÉTHODE
    public Portfolio create(Portfolio portfolio, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        portfolio.setUser(user);
        return portfolioRepository.save(portfolio);
    }

    public Portfolio update(Long id, Portfolio updated) {
        Portfolio existing = getById(id);
        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setLien(updated.getLien());
        existing.setFichier(updated.getFichier());
        return portfolioRepository.save(existing);
    }

    public void delete(Long id) {
        portfolioRepository.deleteById(id);
    }
}