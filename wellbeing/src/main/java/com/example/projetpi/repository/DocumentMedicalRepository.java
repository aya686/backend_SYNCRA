package com.example.projetpi.repository;

import com.example.projetpi.entity.DocumentMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {
    List<DocumentMedical> findByDossierSanteId(Long dossierId);
}
