package com.example.projetpi.repository;

import com.example.projetpi.entity.Antecedent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AntecedentRepository extends JpaRepository<Antecedent, Long> {
    List<Antecedent> findByDossierSanteId(Long dossierId);
}
