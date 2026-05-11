package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.SousTache;
import java.util.List;

public interface SousTacheRepository extends JpaRepository<SousTache, Long> {
    List<SousTache> findByTacheId(Long tacheId);
    List<SousTache> findByAssigneId(Long assigneId);

}