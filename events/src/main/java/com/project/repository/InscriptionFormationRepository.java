// com/project/repository/InscriptionFormationRepository.java
package com.project.repository;

import com.project.entity.InscriptionFormation;
import com.project.entity.ParticipantFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionFormationRepository extends JpaRepository<InscriptionFormation, Long> {

    // ✅ CORRIGER : utiliser participantFormation.participantFormationId
    @Query("SELECT i FROM InscriptionFormation i WHERE i.participantFormation.participantFormationId = :participantId")
    List<InscriptionFormation> findByParticipantId(@Param("participantId") Long participantId);

    List<InscriptionFormation> findByFormationId(Long formationId);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM InscriptionFormation i " +
            "WHERE i.participantFormation.participantFormationId = :participantId AND i.formationId = :formationId")
    boolean existsByParticipantIdAndFormationId(@Param("participantId") Long participantId, @Param("formationId") Long formationId);
}