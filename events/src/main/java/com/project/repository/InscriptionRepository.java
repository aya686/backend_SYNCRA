package com.project.repository;

import com.project.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    // ========== MÉTHODES EXISTANTES ==========
    List<Inscription> findByEvent_EvenementId(Long eventId);
    List<Inscription> findByParticipant_ParticipantId(Long participantId);
    long countByEvent_EvenementId(Long eventId);

    // ========== STATISTIQUES ==========

    // Compter les présences
    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.present = true")
    long countByPresentTrue();

    // Compter par période
    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.dateInscription BETWEEN :start AND :end")
    long countByDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Compter les présences par période
    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.present = true AND i.dateInscription BETWEEN :start AND :end")
    long countPresencesByDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Compter les participants par période
    @Query("SELECT COUNT(DISTINCT i.participant) FROM Inscription i WHERE i.dateInscription BETWEEN :start AND :end")
    long countParticipantsByDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Inscriptions par type d'événement
    @Query("SELECT e.type, COUNT(i) FROM Inscription i JOIN i.event e GROUP BY e.type")
    List<Object[]> countInscriptionsByEventType();

    // Inscriptions par événement
    @Query("SELECT e.titre, COUNT(i) FROM Inscription i JOIN i.event e GROUP BY e.titre")
    List<Object[]> countInscriptionsByEvent();

    // Participants par jour de semaine
    @Query("SELECT FUNCTION('DAYOFWEEK', i.dateInscription), COUNT(DISTINCT i.participant) FROM Inscription i GROUP BY FUNCTION('DAYOFWEEK', i.dateInscription)")
    List<Object[]> countParticipantsByWeekday();

    // Heatmap: participants par jour et heure
    @Query("SELECT FUNCTION('DAYOFWEEK', i.dateInscription) as day, FUNCTION('HOUR', i.dateInscription) as hour, COUNT(i) FROM Inscription i GROUP BY day, hour")
    List<Object[]> countParticipantsByDayAndHour();

    long countByEvent_EvenementIdAndStatut(Long eventId, String statut);
}