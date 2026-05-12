// src/main/java/com/project/repository/TrainingDataRepository.java
package com.project.repository;

import com.project.entity.TrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingDataRepository extends JpaRepository<TrainingData, Long> {

    // Récupérer toutes les données d'entraînement triées par date
    List<TrainingData> findAllByOrderByCreatedAtDesc();

    // Récupérer les données pour un type d'utilisateur spécifique
    List<TrainingData> findByUserType(String userType);

    // Compter le nombre d'exemples par type d'événement
    @Query("SELECT t.eventType, COUNT(t) FROM TrainingData t GROUP BY t.eventType")
    List<Object[]> countByEventType();

    // ✅ CORRECTION: Récupérer les données récentes (derniers 30 jours)
    // Utiliser CURRENT_DATE - 30 DAYS (MySQL)
    @Query(value = "SELECT * FROM training_data WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)", nativeQuery = true)
    List<TrainingData> findRecentData();

    // ✅ Alternative: Version HQL compatible avec LocalDateTime
    @Query("SELECT t FROM TrainingData t WHERE t.createdAt >= :since")
    List<TrainingData> findDataSince(@Param("since") LocalDateTime since);

    // Compter les choix positifs pour un événement
    long countByEventIdAndDidChooseTrue(Long eventId);

    // Compter les choix négatifs pour un événement
    long countByEventIdAndDidChooseFalse(Long eventId);

    // ✅ Récupérer les données d'entraînement pour l'IA (toutes)
    List<TrainingData> findAll();
}