package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.ms2.entities.SessionTravail;

import java.time.LocalDate;
import java.util.List;

public interface SessionTravailRepository extends JpaRepository<SessionTravail, Long> {
    List<SessionTravail> findByTacheId(Long tacheId);
    List<SessionTravail> findByUtilisateurId(Long utilisateurId);
    List<SessionTravail> findByNiveauChargeGreaterThan(Integer seuil);
    @Query("SELECT s FROM SessionTravail s WHERE s.tache.projet.id = :projetId AND CAST(s.debut AS date) >= :date")
    List<SessionTravail> findByProjetIdAndDateAfter(
            @Param("projetId") Long projetId,
            @Param("date") LocalDate date);

}