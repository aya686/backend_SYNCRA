package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "planning")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    @OneToOne
    @JoinColumn(name = "sprint_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Sprint sprint;

    private Double charge;
    private Double capacite;
    private LocalDate dateGeneration;
}
