package com.example.projetpi.controller;
import com.example.projetpi.entity.ArticleSante;
import com.example.projetpi.service.ArticleSanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/articlesante")
@CrossOrigin
public class ArticleSanteController {
    @Autowired
    private ArticleSanteService articleSanteService;
    @PostMapping
    public ArticleSante create(@RequestBody ArticleSante articleSante) {
        return articleSanteService.create(articleSante);
    }
    @GetMapping
    public List<ArticleSante> findAll() {
        return articleSanteService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<ArticleSante> findById(@PathVariable Long id) {
        return articleSanteService.findById(id);
    }
    @PutMapping("/{id}")
    public ArticleSante update(@PathVariable Long id, @RequestBody ArticleSante articleSante) {
        return articleSanteService.update(id, articleSante);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        articleSanteService.delete(id);
    }
}
