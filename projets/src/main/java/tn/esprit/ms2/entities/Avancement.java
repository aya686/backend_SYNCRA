package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "avancement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avancement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "projet_id", nullable = false, unique = true)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    private Double pourcentage;
    private LocalDate dateCalcul;

    @Column(length = 2000)
    private String notesSuivi;
}
