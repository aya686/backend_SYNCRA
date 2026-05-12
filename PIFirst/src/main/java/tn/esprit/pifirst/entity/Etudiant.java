package tn.esprit.pifirst.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "etudiants")
@PrimaryKeyJoinColumn(name = "id_user")
public class Etudiant extends User {

    private String universite;
    private String filiere;
    private String anneeEtude;
    private String emailUniversitaire; // email académique distinct de l'email courant
    private LocalDateTime dateFinAccesGratuit;
}