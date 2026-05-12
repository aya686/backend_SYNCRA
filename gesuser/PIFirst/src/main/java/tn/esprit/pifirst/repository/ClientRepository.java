package tn.esprit.pifirst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pifirst.entity.Client;
import tn.esprit.pifirst.enums.TypeClient;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByType(TypeClient type);
}