package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}