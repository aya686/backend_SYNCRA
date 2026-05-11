package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_travail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;     // référence MS1

    @ManyToOne
    @JoinColumn(name = "tache_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Tache tache;

    private LocalDateTime debut;
    private LocalDateTime fin;
    private Integer dureeMinutes;
    private Integer niveauCharge;   // 1 à 10
}
