package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle;
    private String description;
    private String icone;
    private Integer seuilObtention;
}