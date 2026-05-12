package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Portfolio;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // AJOUTER CETTE LIGNE
    List<Portfolio> findByUserId(Long idUser);
}