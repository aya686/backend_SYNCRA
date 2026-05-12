package org.example.backend_pi.repository;


import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findBySupplierId(Long supplierId);
    // Recherche par catégorie
    List<Machine> findByCategory(CategoryType category);
    List<Machine> findByAvailability(AvailabilityStatus availability);

    List<Machine> findByType(MachineType type);
    // Recherche avec filtres
    @Query("SELECT m FROM Machine m WHERE " +
            "(:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR m.category = :category) AND " +
            "(:location IS NULL OR LOWER(m.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR m.price <= :maxPrice)")
    List<Machine> searchWithFilters(@Param("keyword") String keyword,
                                    @Param("category") CategoryType category,
                                    @Param("location") String location,
                                    @Param("minPrice") Double minPrice,
                                    @Param("maxPrice") Double maxPrice);
    List<Machine> findByValidationStatus(String validationStatus);

    List<Machine> findByValidationStatusAndSupplierId(String validationStatus, Long supplierId);

    // Modifier les méthodes existantes pour ne retourner que les machines APPROVED
    // Optionnel: créer des méthodes spécifiques pour le frontend public
    @Query("SELECT m FROM Machine m WHERE m.validationStatus = 'APPROVED' AND m.isInApp = true")
    List<Machine> findPublicMachines();
}