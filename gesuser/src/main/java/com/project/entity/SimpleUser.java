package com.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;

// Entity
@Entity
@Table(name = "simple_user")
public class SimpleUser {
    @Id
    private Long id;
    private String nom;
    private String role;
    // getters, setters, constructeurs
}