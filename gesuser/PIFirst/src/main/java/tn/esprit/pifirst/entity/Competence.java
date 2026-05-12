package tn.esprit.pifirst.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.Niveau;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "competences")
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle;

    @Enumerated(EnumType.STRING)
    private Niveau niveau;

    private String categorie;

    // MODIFIER : remplacer "freelancer" par "user"
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
}