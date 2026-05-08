package tn.esprit.pifirst.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "id_user")
public class Admin extends User {
    // Pas d'attributs supplémentaires
    // L'admin se distingue uniquement par son entrée dans UserRole
}