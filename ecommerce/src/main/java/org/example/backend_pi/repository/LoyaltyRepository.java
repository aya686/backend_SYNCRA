package org.example.backend_pi.repository;
// repository/LoyaltyRepository.java

import org.example.backend_pi.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoyaltyRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}