package com.example.projetpi.controller;
import com.example.projetpi.entity.RessourceBienetre;
import com.example.projetpi.service.RessourceBienetreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/ressourcebienetre")
@CrossOrigin
public class RessourceBienetreController {
    @Autowired
    private RessourceBienetreService ressourceBienetreService;
    @PostMapping
    public RessourceBienetre create(@RequestBody RessourceBienetre ressourceBienetre) {
        return ressourceBienetreService.create(ressourceBienetre);
    }
    @GetMapping
    public List<RessourceBienetre> findAll() {
        return ressourceBienetreService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<RessourceBienetre> findById(@PathVariable Long id) {
        return ressourceBienetreService.findById(id);
    }
    @PutMapping("/{id}")
    public RessourceBienetre update(@PathVariable Long id, @RequestBody RessourceBienetre ressourceBienetre) {
        return ressourceBienetreService.update(id, ressourceBienetre);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ressourceBienetreService.delete(id);
    }
}
