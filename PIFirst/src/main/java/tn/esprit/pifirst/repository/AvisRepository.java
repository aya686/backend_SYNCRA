package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pifirst.entity.Avis;

import java.util.List;

public interface AvisRepository
        extends JpaRepository<Avis, Long> {

    List<Avis> findByCibleId(Long idCible);
    List<Avis> findByAuteurId(Long idAuteur);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.cible.id = :idCible")
    Double findAvgNoteByCibleId(@Param("idCible") Long idCible);
}