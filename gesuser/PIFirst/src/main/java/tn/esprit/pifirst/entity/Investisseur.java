package tn.esprit.pifirst.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "investisseurs")
@PrimaryKeyJoinColumn(name = "id_user")
public class Investisseur extends User {

    private String domainesInteret;
    private Double budgetMin;
    private Double budgetMax;
    private Integer nbProjetsFinances;
    private String description;
}