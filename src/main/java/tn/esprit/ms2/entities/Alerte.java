package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "alerte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    @JsonIgnore
    private Projet projet;

    // Champ calculé pour le JSON
    @Transient
    public Long getProjetId() {
        return projet != null ? projet.getId() : null;
    }

    @Enumerated(EnumType.STRING)
    private TypeAlerte type;

    @Column(length = 1000)
    private String message;

    private LocalDate date;

    @Column(nullable = false)
    private Boolean traitee = false;
}