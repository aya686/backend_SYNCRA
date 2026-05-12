package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pifirst.enums.Statut;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nom;
    private String prenom;
    private String photo;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private LocalDateTime dateInscription;
    private LocalDateTime derniereConnexion;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Portfolio> portfolios;

    @Column(name = "totp_secret", nullable = true, length = 100)
    private String totpSecret;

    @Column(name = "blocked_at", nullable = true)
    private LocalDateTime blockedAt;

    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }
}