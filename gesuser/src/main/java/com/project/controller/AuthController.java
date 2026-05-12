package com.project.controller;

import com.project.entity.SimpleUser;
import com.project.repository.SimpleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// Ajoutez ce contrôleur dans votre backend
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private SimpleUserRepository repository;

    @GetMapping("/simple-user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<SimpleUser> user = repository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }
}