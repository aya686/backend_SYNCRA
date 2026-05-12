package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.entity.Programme;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
}