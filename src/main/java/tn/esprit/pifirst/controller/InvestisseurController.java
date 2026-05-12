package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Investisseur;
import tn.esprit.pifirst.service.InvestisseurService;
import java.util.List;

@RestController
@RequestMapping("/api/investisseurs")
public class InvestisseurController {

    @Autowired
    private InvestisseurService investisseurService;

    @GetMapping
    public List<Investisseur> getAll() {
        return investisseurService.getAll();
    }

    @GetMapping("/{id}")
    public Investisseur getById(@PathVariable Long id) {
        return investisseurService.getById(id);
    }

    @GetMapping("/budget/{montant}")
    public List<Investisseur> getParBudgetMin(@PathVariable Double montant) {
        return investisseurService.getParBudgetMin(montant);
    }

    @PostMapping("/user/{idUser}")
    public Investisseur create(@PathVariable Long idUser, @RequestBody Investisseur investisseur) {
        return investisseurService.create(investisseur, idUser);
    }

    @PutMapping("/{id}")
    public Investisseur update(@PathVariable Long id, @RequestBody Investisseur investisseur) {
        return investisseurService.update(id, investisseur);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        investisseurService.delete(id);
    }
}