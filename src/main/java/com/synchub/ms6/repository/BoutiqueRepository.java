package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Boutique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoutiqueRepository extends JpaRepository<Boutique, Long> {
    List<Boutique> findByStatut(Boutique.StatutBoutique statut);
}
