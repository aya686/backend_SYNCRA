package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Badge;
import tn.esprit.pifirst.service.BadgeService;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @GetMapping
    public List<Badge> getAll() {
        return badgeService.getAll();
    }

    @GetMapping("/{id}")
    public Badge getById(@PathVariable Long id) {
        return badgeService.getById(id);
    }

    @PostMapping
    public Badge create(@RequestBody Badge badge) {
        return badgeService.create(badge);
    }

    @PutMapping("/{id}")
    public Badge update(@PathVariable Long id,
                        @RequestBody Badge badge) {
        return badgeService.update(id, badge);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        badgeService.delete(id);
    }
}
