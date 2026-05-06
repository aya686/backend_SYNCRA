package org.example.backend_pi.repository;
// repository/LoyaltyTransactionRepository.java

import org.example.backend_pi.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LoyaltyTransaction> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}