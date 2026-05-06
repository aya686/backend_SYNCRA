package org.example.backend_pi.repository;

// repository/ServiceRepository.java

import org.example.backend_pi.entity.ServiceEntity;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    // Recherches existantes
    List<ServiceEntity> findByProviderId(Long providerId);
    List<ServiceEntity> findByCategory(CategoryType category);
    List<ServiceEntity> findByAvailability(AvailabilityStatus availability);
    List<ServiceEntity> findByServiceType(ServiceType serviceType);

    // ✅ AJOUTER CES MÉTHODES POUR LA VALIDATION
    List<ServiceEntity> findByValidationStatus(String validationStatus);

    @Query("SELECT s FROM ServiceEntity s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR s.category = :category) AND " +
            "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minPrice IS NULL OR s.basePrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR s.basePrice <= :maxPrice)")
    List<ServiceEntity> searchWithFilters(@Param("keyword") String keyword,
                                          @Param("category") CategoryType category,
                                          @Param("location") String location,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice);
}