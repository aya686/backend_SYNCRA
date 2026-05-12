package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "objectif")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objectif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    private String titre;

    @Column(length = 1000)
    private String description;

    private Boolean atteint = false;
}
