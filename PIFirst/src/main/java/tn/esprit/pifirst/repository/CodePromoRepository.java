package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.CodePromo;
import tn.esprit.pifirst.enums.StatutCodePromo;
import java.util.Optional;

public interface CodePromoRepository extends JpaRepository<CodePromo, Long> {
    Optional<CodePromo> findByCode(String code);
    Optional<CodePromo> findByCodeAndStatut(String code, StatutCodePromo statut);
}