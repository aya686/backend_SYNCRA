package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Avis;
import tn.esprit.pifirst.service.AvisService;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    @Autowired
    private AvisService avisService;

    @GetMapping
    public List<Avis> getAll() {
        return avisService.getAll();
    }

    @GetMapping("/{id}")
    public Avis getById(@PathVariable Long id) {
        return avisService.getById(id);
    }

    @GetMapping("/cible/{idCible}")
    public List<Avis> getByCible(@PathVariable Long idCible) {
        return avisService.getByCible(idCible);
    }

    @GetMapping("/score/{idCible}")
    public Double getScoreMoyen(@PathVariable Long idCible) {
        return avisService.getScoreMoyen(idCible);
    }

    @PostMapping("/{idAuteur}/{idCible}")
    public Avis create(@PathVariable Long idAuteur,
                       @PathVariable Long idCible,
                       @RequestBody Avis avis) {
        return avisService.create(idAuteur, idCible, avis);
    }

    @PutMapping("/repondre/{id}")
    public Avis repondre(@PathVariable Long id,
                         @RequestParam String reponse) {
        return avisService.repondre(id, reponse);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        avisService.delete(id);
    }
}