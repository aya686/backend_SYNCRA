package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "idee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Idee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long auteurId;          // référence MS1

    private String titre;

    @Column(length = 2000)
    private String description;

    private String categorie;
    @Enumerated(EnumType.STRING)
    private StatutIdee statut;
    @ManyToOne
    @JoinColumn(name = "projet_id")
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;          // null si pas encore transformée
}