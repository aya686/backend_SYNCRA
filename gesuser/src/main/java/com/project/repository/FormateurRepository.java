package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.entity.Formateur;

public interface FormateurRepository extends JpaRepository<Formateur, Long> {
}