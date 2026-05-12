package com.synchub.ms6.repository;

import com.synchub.ms6.entity.RouteLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteLivraisonRepository extends JpaRepository<RouteLivraison, Long> {

    List<RouteLivraison> findByStatut(RouteLivraison.StatutRoute statut);

    List<RouteLivraison> findByTransporteur(String transporteur);

    List<RouteLivraison> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT r FROM RouteLivraison r WHERE r.statut IN :statuts ORDER BY r.dateOptimisation DESC")
    List<RouteLivraison> findByStatutsOrderByDateOptimisationDesc(@Param("statuts") List<RouteLivraison.StatutRoute> statuts);

    @Query("SELECT r FROM RouteLivraison r LEFT JOIN FETCH r.points WHERE r.routeId = :id")
    Optional<RouteLivraison> findByIdWithPoints(@Param("id") Long id);

    @Query("SELECT COUNT(r) FROM RouteLivraison r WHERE r.statut = :statut")
    Long countByStatut(@Param("statut") RouteLivraison.StatutRoute statut);

    @Query("SELECT AVG(r.distanceTotaleKm) FROM RouteLivraison r WHERE r.dateCreation >= :dateDebut")
    Double calculateAverageDistanceSince(@Param("dateDebut") LocalDateTime dateDebut);

    @Query("SELECT SUM(r.distanceTotaleKm) FROM RouteLivraison r " +
           "WHERE r.dateCreation BETWEEN :debut AND :fin AND r.statut = 'TERMINEE'")
    Double sumDistanceByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT r.transporteur, COUNT(r), SUM(r.distanceTotaleKm) " +
           "FROM RouteLivraison r " +
           "WHERE r.dateCreation >= :dateDebut " +
           "GROUP BY r.transporteur")
    List<Object[]> getStatsByTransporteur(@Param("dateDebut") LocalDateTime dateDebut);
}
