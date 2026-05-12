package com.example.projetpi.repository;

import com.example.projetpi.entity.DossierSante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DossierSanteRepository extends JpaRepository<DossierSante, Long> {
    Optional<DossierSante> findByUtilisateurId(Long utilisateurId);
}