package com.synchub.ms6.repository;

import com.synchub.ms6.entity.StatsBoutique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatsBoutiqueRepository extends JpaRepository<StatsBoutique, Long> {
    List<StatsBoutique> findByBoutiqueBoutiqueId(Long boutiqueId);
    Optional<StatsBoutique> findTopByBoutiqueBoutiqueIdOrderByDateCalculDesc(Long boutiqueId);
}
