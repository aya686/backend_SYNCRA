package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.TypeClient;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "clients")
@PrimaryKeyJoinColumn(name = "id_user")
public class Client extends User {

    @Enumerated(EnumType.STRING)
    private TypeClient type; // PARTICULIER, ENTREPRISE

    private String nomEntreprise;    // null si particulier
    private String secteurActivite;
    private String description;
    private String siteWeb;          // null si particulier
}