package org.example.backend_pi.controller;


import org.example.backend_pi.enums.CategoryType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    // Récupérer toutes les catégories disponibles pour le filtrage
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllCategories() {
        List<Map<String, String>> categories = Arrays.stream(CategoryType.values())
                .map(category -> Map.of(
                        "name", category.name(),
                        "label", category.getLabel(),
                        "description", category.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}