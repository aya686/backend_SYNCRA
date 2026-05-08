package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Badge;

public interface BadgeRepository
        extends JpaRepository<Badge, Long> {

}
