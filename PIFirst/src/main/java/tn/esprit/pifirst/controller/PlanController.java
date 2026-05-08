package tn.esprit.pifirst.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Plan;
import tn.esprit.pifirst.service.PlanService;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public List<Plan> getAll() {
        return planService.getAll();
    }

    @GetMapping("/{id}")
    public Plan getById(@PathVariable Long id) {
        return planService.getById(id);
    }

    @GetMapping("/{id}/limite/{cle}")
    public Integer getLimite(@PathVariable Long id, @PathVariable String cle) {
        return planService.getLimiteValue(id, cle);
    }

    @PostMapping
    public Plan create(@RequestBody Plan plan) {
        return planService.create(plan);
    }

    @PutMapping("/{id}")
    public Plan update(@PathVariable Long id, @RequestBody Plan plan) {
        return planService.update(id, plan);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        planService.delete(id);
    }
}