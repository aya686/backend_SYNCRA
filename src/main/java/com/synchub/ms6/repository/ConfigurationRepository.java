package com.synchub.ms6.repository;

import com.synchub.ms6.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Optional<Configuration> findByBoutiqueBoutiqueId(Long boutiqueId);
}
