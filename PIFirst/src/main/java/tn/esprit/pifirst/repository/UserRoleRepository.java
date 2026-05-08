package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.UserRole;
import tn.esprit.pifirst.enums.StatutRole;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long idUser);
    List<UserRole> findByUserIdAndStatut(Long idUser, StatutRole statut);
    Optional<UserRole> findByUserIdAndRoleIdAndStatut(Long idUser, Long idRole, StatutRole statut);
}