package com.synchub.ms6.repository;

import com.synchub.ms6.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour les comportements utilisateurs
 * Utilisé par le système de recommandation collaboratif
 */
@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    List<UserBehavior> findByUserId(Long userId);

    List<UserBehavior> findByProduitId(Long produitId);

    List<UserBehavior> findByUserIdAndProduitId(Long userId, Long produitId);

    List<UserBehavior> findByType(String type);

    List<UserBehavior> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.userId = :userId AND ub.timestamp >= :since")
    List<UserBehavior> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT ub FROM UserBehavior ub WHERE ub.produitId = :produitId AND ub.timestamp >= :since")
    List<UserBehavior> findRecentByProduitId(@Param("produitId") Long produitId, @Param("since") LocalDateTime since);

    @Query("SELECT ub.type, COUNT(ub) FROM UserBehavior ub WHERE ub.produitId = :produitId GROUP BY ub.type")
    List<Object[]> getBehaviorStatsByProduct(@Param("produitId") Long produitId);
}
