// com/project/repository/FavorisRepository.java
package com.project.repository;

import com.project.entity.Favoris;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavorisRepository extends JpaRepository<Favoris, Long> {
    List<Favoris> findByUserId(Long userId);
    boolean existsByUserIdAndEvenementId(Long userId, Long evenementId);
    void deleteByUserIdAndEvenementId(Long userId, Long evenementId);
}