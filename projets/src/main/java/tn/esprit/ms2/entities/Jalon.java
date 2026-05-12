package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "jalon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    private String titre;
    private LocalDate dateEcheance;
    private Boolean atteint = false;
}
