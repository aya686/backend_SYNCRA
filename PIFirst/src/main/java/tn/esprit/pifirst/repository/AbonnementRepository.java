package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Abonnement;
import tn.esprit.pifirst.enums.StatutAbonnement;
import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByUserId(Long idUser);

    Optional<Abonnement> findByUserIdAndStatut(Long idUser, StatutAbonnement statut);

    // Vérifier si un user a déjà utilisé un code promo spécifique
    boolean existsByUserIdAndCodePromoId(Long userId, Long codePromoId);
}