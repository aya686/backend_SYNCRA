package com.example.projetpi.repository;

import com.example.projetpi.entity.AlerteBurnout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteBurnoutRepository extends JpaRepository<AlerteBurnout, Long> {

    // Utilisé dans : getAlertesByUtilisateur() + getDashboardSummary()
    List<AlerteBurnout> findByUtilisateurId(Long utilisateurId);
}