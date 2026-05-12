package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Portfolio;
import tn.esprit.pifirst.service.PortfolioService;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public List<Portfolio> getAll() {
        return portfolioService.getAll();
    }

    @GetMapping("/{id}")
    public Portfolio getById(@PathVariable Long id) {
        return portfolioService.getById(id);
    }

    // AJOUTER CET ENDPOINT
    @GetMapping("/user/{idUser}")
    public List<Portfolio> getByUser(@PathVariable Long idUser) {
        return portfolioService.getByUser(idUser);
    }

    // AJOUTER CET ENDPOINT
    @PostMapping("/user/{idUser}")
    public Portfolio create(@PathVariable Long idUser, @RequestBody Portfolio portfolio) {
        return portfolioService.create(portfolio, idUser);
    }

    @PutMapping("/{id}")
    public Portfolio update(@PathVariable Long id, @RequestBody Portfolio portfolio) {
        return portfolioService.update(id, portfolio);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        portfolioService.delete(id);
    }
}