package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "freelancers")
@PrimaryKeyJoinColumn(name = "id_user")
public class Freelancer extends User {

    private String description;
    private String cv;
    private Boolean disponible = true;

    // SUPPRIME CES LIGNES :
    // @JsonManagedReference
    // @OneToMany(mappedBy = "freelancer", cascade = CascadeType.ALL)
    // private List<Competence> competences;
    //
    // @OneToMany(mappedBy = "freelancer", cascade = CascadeType.ALL)
    // private List<Portfolio> portfolio;
}