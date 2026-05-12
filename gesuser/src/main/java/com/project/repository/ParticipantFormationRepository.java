package com.project.repository;

import com.project.entity.ParticipantFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParticipantFormationRepository extends JpaRepository<ParticipantFormation, Long> {
    Optional<ParticipantFormation> findByEmail(String email);

}