package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sous_tache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SousTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tache_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Tache tache;

    private String titre;
    @Enumerated(EnumType.STRING)
    private StatutSousTache statut;
    private Long assigneId;         // référence MS1
}