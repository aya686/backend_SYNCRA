package tn.esprit.pifirst.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pifirst.entity.LoginHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // ============================================================
    // MÉTHODES EXISTANTES
    // ============================================================

    // Récupérer l'historique d'un utilisateur (du plus récent au plus ancien)
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    // Récupérer uniquement les connexions réussies d'un utilisateur
    List<LoginHistory> findByUserIdAndIsSuccessfulTrueOrderByLoginTimeDesc(Long userId);

    // Compter les tentatives échouées dans les dernières 24h
    long countByUserIdAndIsSuccessfulFalseAndLoginTimeAfter(Long userId, LocalDateTime since);

    // Dernière connexion réussie d'un utilisateur
    @Query("SELECT l FROM LoginHistory l WHERE l.user.id = :userId AND l.isSuccessful = true ORDER BY l.loginTime DESC LIMIT 1")
    Optional<LoginHistory> findLastSuccessfulLogin(@Param("userId") Long userId);

    // Compter les connexions depuis une IP (pour détection anormale)
    long countByIpAddressAndLoginTimeAfter(String ipAddress, LocalDateTime since);

    // Récupérer le dernier log 2FA non validé
    Optional<LoginHistory> findTopByUserIdAndTwoFactorRequiredTrueAndTwoFactorValidatedFalseOrderByLoginTimeDesc(Long userId);

    // ✅ Récupérer les logs d'échec d'un utilisateur (pour réinitialisation)
    List<LoginHistory> findByUserIdAndIsSuccessfulFalseOrderByLoginTimeDesc(Long userId);

    // ============================================================
    // MÉTHODES POUR LE DASHBOARD ADMIN
    // ============================================================

    // Compter les connexions depuis une date
    long countByLoginTimeAfter(LocalDateTime date);

    // Compter les 2FA depuis une date
    long countByTwoFactorRequiredTrueAndLoginTimeAfter(LocalDateTime date);

    // Compter les décisions BLOCKED depuis une date
    long countByMlDecisionAndLoginTimeAfter(String decision, LocalDateTime date);

    // Score ML moyen
    @Query("SELECT AVG(l.riskScore) FROM LoginHistory l WHERE l.mlDecision IS NOT NULL AND l.riskScore IS NOT NULL")
    Double getAverageRiskScoreForMlDecisions();

    // Dernière connexion échouée d'un utilisateur
    Optional<LoginHistory> findTopByUserIdAndIsSuccessfulFalseOrderByLoginTimeDesc(Long userId);

    // N dernières connexions
    @Query("SELECT l FROM LoginHistory l ORDER BY l.loginTime DESC")
    List<LoginHistory> findTopNOrderByLoginTimeDesc(PageRequest pageRequest);

    default List<LoginHistory> findTopNOrderByLoginTimeDesc(int limit) {
        return findTopNOrderByLoginTimeDesc(PageRequest.of(0, limit));
    }

    // Connexions depuis une date (pour distribution)
    List<LoginHistory> findByLoginTimeAfter(LocalDateTime date);
}