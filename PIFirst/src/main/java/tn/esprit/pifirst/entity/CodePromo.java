package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.StatutCodePromo;
import java.time.LocalDateTime;

@Entity
@Table(name = "codes_promo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodePromo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private Integer reductionPct;           // 10, 20, 30 (%)
    private LocalDateTime dateExpiration;   // nullable
    private Integer nbUtilisationsMax;      // nullable
    private Integer nbUtilisationsCourant;

    @Enumerated(EnumType.STRING)
    private StatutCodePromo statut;

    // Constructeur avec valeurs par défaut
    public CodePromo(String code, Integer reductionPct, LocalDateTime dateExpiration, Integer nbUtilisationsMax) {
        this.code = code;
        this.reductionPct = reductionPct;
        this.dateExpiration = dateExpiration;
        this.nbUtilisationsMax = nbUtilisationsMax;
        this.nbUtilisationsCourant = 0;
        this.statut = StatutCodePromo.ACTIF;
    }
}