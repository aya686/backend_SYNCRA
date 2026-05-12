package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Freelancer;
import tn.esprit.pifirst.service.FreelancerService;
import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
public class FreelancerController {

    @Autowired
    private FreelancerService freelancerService;

    @GetMapping
    public List<Freelancer> getAll() {
        return freelancerService.getAll();
    }

    @GetMapping("/{id}")
    public Freelancer getById(@PathVariable Long id) {
        return freelancerService.getById(id);
    }

    @GetMapping("/disponibles")
    public List<Freelancer> getDisponibles() {
        return freelancerService.getDisponibles();
    }

    @PostMapping("/user/{idUser}")
    public Freelancer create(@PathVariable Long idUser, @RequestBody Freelancer freelancer) {
        return freelancerService.create(freelancer, idUser);
    }

    @PutMapping("/{id}")
    public Freelancer update(@PathVariable Long id, @RequestBody Freelancer freelancer) {
        return freelancerService.update(id, freelancer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        freelancerService.delete(id);
    }
}