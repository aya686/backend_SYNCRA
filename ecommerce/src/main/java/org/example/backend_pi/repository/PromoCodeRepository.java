package org.example.backend_pi.repository;
// repository/PromoCodeRepository.java

import org.example.backend_pi.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    /** Tous les codes actifs d'un utilisateur */
    List<PromoCode> findByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    /** Tous les codes (actifs + utilisés) d'un utilisateur */
    List<PromoCode> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByCode(String code);
}