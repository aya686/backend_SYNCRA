package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "sprint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double chargeTotal;
    @Enumerated(EnumType.STRING)
    private StatutSprint statut;
    @OneToOne(mappedBy = "sprint", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private Planning planning;
}
