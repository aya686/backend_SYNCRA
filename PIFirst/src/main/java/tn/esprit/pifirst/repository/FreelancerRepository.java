package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Freelancer;

import java.util.List;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
    List<Freelancer> findByDisponibleTrue();
}