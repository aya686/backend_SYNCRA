package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.enums.Statut;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByStatut(Statut statut);
    List<User> findByStatut(Statut statut);

    List<User> findByStatutAndBlockedAtBefore(Statut statut, LocalDateTime date);
}