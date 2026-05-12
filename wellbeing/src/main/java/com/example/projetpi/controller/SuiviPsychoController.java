package com.example.projetpi.controller;
import com.example.projetpi.entity.SuiviPsycho;
import com.example.projetpi.service.SuiviPsychoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/suivipsycho")
@CrossOrigin
public class SuiviPsychoController {
    @Autowired
    private SuiviPsychoService suiviPsychoService;
    @PostMapping
    public SuiviPsycho create(@RequestBody SuiviPsycho suiviPsycho) {
        return suiviPsychoService.create(suiviPsycho);
    }
    @GetMapping
    public List<SuiviPsycho> findAll() {
        return suiviPsychoService.findAll();
    }
    @GetMapping("/{id}")
    public Optional<SuiviPsycho> findById(@PathVariable Long id) {
        return suiviPsychoService.findById(id);
    }
    @PutMapping("/{id}")
    public SuiviPsycho update(@PathVariable Long id, @RequestBody SuiviPsycho suiviPsycho) {
        return suiviPsychoService.update(id, suiviPsycho);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        suiviPsychoService.delete(id);
    }
}
