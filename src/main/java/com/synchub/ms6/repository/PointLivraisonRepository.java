package com.synchub.ms6.repository;

import com.synchub.ms6.entity.PointLivraison;
import com.synchub.ms6.entity.RouteLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointLivraisonRepository extends JpaRepository<PointLivraison, Long> {

    List<PointLivraison> findByRouteOrderByOrdreAsc(RouteLivraison route);

    List<PointLivraison> findByRouteRouteIdOrderByOrdreAsc(Long routeId);

    List<PointLivraison> findByStatut(PointLivraison.StatutPoint statut);

    Optional<PointLivraison> findByLivraisonLivraisonId(Long livraisonId);

    @Query("SELECT p FROM PointLivraison p WHERE p.route.routeId = :routeId ORDER BY p.ordre")
    List<PointLivraison> findByRouteIdWithOrder(@Param("routeId") Long routeId);

    @Query("SELECT COUNT(p) FROM PointLivraison p WHERE p.route.routeId = :routeId AND p.statut = :statut")
    Long countByRouteIdAndStatut(@Param("routeId") Long routeId, @Param("statut") PointLivraison.StatutPoint statut);

    @Query("SELECT AVG(p.distancePrecedentKm) FROM PointLivraison p WHERE p.route.routeId = :routeId")
    Double calculateAverageDistanceBetweenPoints(@Param("routeId") Long routeId);

    @Query("SELECT p FROM PointLivraison p " +
           "WHERE p.route.statut = 'EN_COURS' AND p.ordre = " +
           "(SELECT MIN(p2.ordre) FROM PointLivraison p2 WHERE p2.route.routeId = p.route.routeId AND p2.statut = 'PLANIFIE')")
    List<PointLivraison> findNextDeliveryPoints();

    @Query("SELECT p FROM PointLivraison p WHERE p.statut = 'NON_LIVRE' AND p.priorite >= :prioriteMin ORDER BY p.priorite DESC")
    List<PointLivraison> findFailedDeliveriesByPriority(@Param("prioriteMin") Integer prioriteMin);
}
