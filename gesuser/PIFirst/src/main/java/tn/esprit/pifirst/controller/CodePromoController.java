package tn.esprit.pifirst.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.CodePromo;
import tn.esprit.pifirst.service.CodePromoService;
import java.util.List;

@RestController
@RequestMapping("/api/codes-promo")
@RequiredArgsConstructor
public class CodePromoController {

    private final CodePromoService codePromoService;

    @GetMapping
    public List<CodePromo> getAll() {
        return codePromoService.getAll();
    }

    @GetMapping("/{id}")
    public CodePromo getById(@PathVariable Long id) {
        return codePromoService.getById(id);
    }

    @GetMapping("/code/{code}")
    public CodePromo getByCode(@PathVariable String code) {
        return codePromoService.getByCode(code);
    }

    @PostMapping
    public CodePromo create(@RequestBody CodePromo codePromo) {
        return codePromoService.create(codePromo);
    }

    @PutMapping("/{id}")
    public CodePromo update(@PathVariable Long id, @RequestBody CodePromo codePromo) {
        return codePromoService.update(id, codePromo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        codePromoService.delete(id);
    }
}