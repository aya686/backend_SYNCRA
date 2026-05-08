package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.UserBadge;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository
        extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long idUser);
    Optional<UserBadge> findByUserIdAndBadgeId(Long idUser, Long idBadge);
}