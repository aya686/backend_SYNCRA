package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Annulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnulationRepository extends JpaRepository<Annulation, Long> {
    Optional<Annulation> findByCommandeCommandeId(Long commandeId);
}
