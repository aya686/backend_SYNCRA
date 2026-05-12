package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Remboursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {
    Optional<Remboursement> findByRetourRetourId(Long retourId);
}
