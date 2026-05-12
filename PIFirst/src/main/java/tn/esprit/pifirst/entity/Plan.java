package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.TypePlan;
import java.util.List;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypePlan type;        // GRATUIT, STANDARD, PREMIUM, ETUDIANT

    private Double prix;          // 0 si gratuit
    private Integer dureeJours;
    private String description;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlanLimite> limites;
}