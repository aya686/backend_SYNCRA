package tn.esprit.ms2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendrier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calendrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projetId;
    private Long evenementId;       // référence événement externe (MS4)

    @ManyToOne
    @JoinColumn(name = "tache_id")
    @ToString.Exclude
    @JsonIgnore
    private Tache tache;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    @Enumerated(EnumType.STRING)
    private TypeCalendrier type;}
