package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "affectation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tache_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Tache tache;

    private Long utilisateurId;     // référence MS1
    private LocalDate dateAffectation;
    private String role;
}
