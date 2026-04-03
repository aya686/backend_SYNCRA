package org.example.backend_pi.repository;

// repository/ReviewRepository.java

import org.example.backend_pi.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMachineId(Long machineId);

    List<Review> findByServiceId(Long serviceId);

    List<Review> findByUserId(Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.machine.id = :machineId")
    Double getAverageRatingForMachine(@Param("machineId") Long machineId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.service.id = :serviceId")
    Double getAverageRatingForService(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.machine.id = :machineId")
    Integer getReviewCountForMachine(@Param("machineId") Long machineId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.service.id = :serviceId")
    Integer getReviewCountForService(@Param("serviceId") Long serviceId);
}