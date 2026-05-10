package com.example.projetpi.repository;

import com.example.projetpi.entity.ProgrammePrevention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammePreventionRepository extends JpaRepository<ProgrammePrevention, Long> {
    List<ProgrammePrevention> findByUtilisateurId(Long utilisateurId);
    Optional<ProgrammePrevention> findByUtilisateurIdAndActifTrue(Long utilisateurId);
}
