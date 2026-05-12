package org.example.backend_pi.controller;
// controller/CategoryController.java — enrichi avec MachineType et ServiceType

import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.MachineType;
import org.example.backend_pi.enums.ServiceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    /** GET /api/categories — Toutes les catégories */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllCategories() {
        List<Map<String, String>> categories = Arrays.stream(CategoryType.values())
                .map(c -> Map.of(
                        "name",        c.name(),
                        "label",       c.getLabel(),
                        "description", c.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    /** GET /api/categories/machine-types — Types de machines */
    @GetMapping("/machine-types")
    public ResponseEntity<List<Map<String, String>>> getMachineTypes() {
        return ResponseEntity.ok(
                Arrays.stream(MachineType.values())
                        .map(t -> Map.of("name", t.name(), "label", t.getLabel()))
                        .collect(Collectors.toList())
        );
    }

    /** GET /api/categories/service-types — Types de services */
    @GetMapping("/service-types")
    public ResponseEntity<List<Map<String, String>>> getServiceTypes() {
        return ResponseEntity.ok(
                Arrays.stream(ServiceType.values())
                        .map(t -> Map.of("name", t.name(), "label", t.getLabel()))
                        .collect(Collectors.toList())
        );
    }
}