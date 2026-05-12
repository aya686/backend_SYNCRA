package tn.esprit.ms2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms2.entities.Projet;
import tn.esprit.ms2.entities.StatutProjet;
import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByPorteurId(Long porteurId);
    List<Projet> findByStatut(StatutProjet statut);
    List<Projet> findByCategorie(String categorie);
    List<Projet> findByMoniteurId(Long moniteurId);
}