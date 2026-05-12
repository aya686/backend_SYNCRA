package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.StatutRole;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role role; // entité Role, plus enum

    @Enumerated(EnumType.STRING)
    private StatutRole statut; // ACTIF, EXPIRE, REVOQUE

    private LocalDateTime dateAttribution;
    private LocalDateTime dateExpiration; // null si permanent

    private String motifChangement;
}