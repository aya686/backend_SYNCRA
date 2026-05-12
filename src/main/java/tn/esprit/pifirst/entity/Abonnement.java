package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.StatutAbonnement;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonnements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    private StatutAbonnement statut;

    private Boolean renouvellementAuto;
    private LocalDateTime derniereActivite;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_plan")
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "id_code_promo", nullable = true)
    private CodePromo codePromo;
}