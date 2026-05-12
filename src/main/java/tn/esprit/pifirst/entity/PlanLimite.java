package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans_limites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanLimite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cle;    // nb_offres_max / nb_boutiques_max / nb_formations_max
    private Integer valeur;

    @ManyToOne
    @JoinColumn(name = "id_plan")
    private Plan plan;
}