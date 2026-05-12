package com.example.projetpi.controller;

import com.example.projetpi.entity.AlerteBurnout;
import com.example.projetpi.service.AlerteBurnoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/alerteburnout")
@CrossOrigin
public class AlerteBurnoutController {
    @Autowired
    private AlerteBurnoutService alerteBurnoutService;

    @PostMapping
    public AlerteBurnout create(@RequestBody AlerteBurnout alerteBurnout) {
        return alerteBurnoutService.create(alerteBurnout);
    }

    @GetMapping
    public List<AlerteBurnout> findAll() {
        return alerteBurnoutService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<AlerteBurnout> findById(@PathVariable Long id) {
        return alerteBurnoutService.findById(id);
    }

    @PutMapping("/{id}")
    public AlerteBurnout update(@PathVariable Long id, @RequestBody AlerteBurnout alerteBurnout) {
        return alerteBurnoutService.update(id, alerteBurnout);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        alerteBurnoutService.delete(id);
    }
}
