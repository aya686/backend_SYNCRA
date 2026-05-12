package tn.esprit.pifirst.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String lien;
    private String fichier;

    // MODIFIER : remplacer "freelancer" par "user"
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
}