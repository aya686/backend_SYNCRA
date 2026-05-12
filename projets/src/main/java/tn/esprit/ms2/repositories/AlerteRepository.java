package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.ms2.entities.Alerte;
import tn.esprit.ms2.entities.TypeAlerte;
import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByUtilisateurId(Long utilisateurId);
    List<Alerte> findByTraitee(boolean traitee);
    List<Alerte> findByType(TypeAlerte type);
    List<Alerte> findByUtilisateurIdAndTraitee(Long utilisateurId, boolean traitee);
    @Query("SELECT a FROM Alerte a WHERE a.projet.id = :projetId")
    List<Alerte> findByProjetId(@Param("projetId") Long projetId);

    @Query("SELECT COUNT(a) > 0 FROM Alerte a WHERE a.projet.id = :projetId AND a.type = :type AND a.traitee = :traitee")
    boolean existsByProjetIdAndTypeAndTraitee(
            @Param("projetId") Long projetId,
            @Param("type") TypeAlerte type,
            @Param("traitee") boolean traitee);

}