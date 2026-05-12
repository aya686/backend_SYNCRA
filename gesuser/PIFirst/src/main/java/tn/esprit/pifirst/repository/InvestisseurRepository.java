package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Investisseur;

import java.util.List;

public interface InvestisseurRepository extends JpaRepository<Investisseur, Long> {
    List<Investisseur> findByBudgetMaxGreaterThanEqual(Double montant);
}