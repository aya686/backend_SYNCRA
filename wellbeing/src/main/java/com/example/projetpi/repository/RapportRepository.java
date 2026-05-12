package com.example.projetpi.repository;
import com.example.projetpi.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RapportRepository extends JpaRepository<Rapport, Long> {
}
