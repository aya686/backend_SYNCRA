package tn.esprit.pifirst.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Abonnement;
import tn.esprit.pifirst.service.AbonnementService;
import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    @GetMapping
    public List<Abonnement> getAll() {
        return abonnementService.getAll();
    }

    @GetMapping("/{id}")
    public Abonnement getById(@PathVariable Long id) {
        return abonnementService.getById(id);
    }

    @GetMapping("/user/{idUser}")
    public List<Abonnement> getByUser(@PathVariable Long idUser) {
        return abonnementService.getByUser(idUser);
    }

    @GetMapping("/user/{idUser}/actif")
    public Abonnement getAbonnementActif(@PathVariable Long idUser) {
        return abonnementService.getAbonnementActif(idUser);
    }

    @PostMapping("/souscrire/{idUser}/{idPlan}")
    public Abonnement souscrire(@PathVariable Long idUser,
                                @PathVariable Long idPlan) {
        return abonnementService.souscrire(idUser, idPlan);
    }

    @PostMapping("/souscrire/{idUser}/{idPlan}/code/{codePromo}")
    public Abonnement souscrireAvecCode(@PathVariable Long idUser,
                                        @PathVariable Long idPlan,
                                        @PathVariable String codePromo) {
        return abonnementService.souscrireAvecCodePromo(idUser, idPlan, codePromo);
    }

    @PutMapping("/{id}/activite")
    public void updateActivite(@PathVariable Long id) {
        abonnementService.updateDerniereActivite(id);
    }

    @GetMapping("/{id}/devrait-downgrade/{jours}")
    public boolean devraitDowngrade(@PathVariable Long id,
                                    @PathVariable int jours) {
        return abonnementService.devraitDowngrade(id, jours);
    }

    @PutMapping("/{id}/resilier")
    public Abonnement resilier(@PathVariable Long id) {
        return abonnementService.resilier(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        abonnementService.delete(id);
    }

    @PutMapping("/{id}")
    public Abonnement update(@PathVariable Long id,
                             @RequestBody Abonnement abonnement) {
        return abonnementService.update(id, abonnement);
    }
}