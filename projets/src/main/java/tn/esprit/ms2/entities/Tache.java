package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "projet_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Projet projet;

    private String titre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private PrioriteTache priorite;

    @Enumerated(EnumType.STRING)
    private StatutTache statut;
    private LocalDate deadline;
    private Double estimationHeures;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private List<SousTache> sousTaches;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private List<Affectation> affectations;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private List<SessionTravail> sessions;
}
