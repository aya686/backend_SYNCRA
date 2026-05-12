package com.example.projetpi.controller;

import com.example.projetpi.entity.DocumentMedical;
import com.example.projetpi.service.DocumentMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/document-medical")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentMedicalController {

    @Autowired
    private DocumentMedicalService documentMedicalService;

    // ── CRUD de base uniquement ───────────────────────────────────────
    // ⚠️ Upload et Delete sont gérés dans DossierSanteController
    // sous /api/documents/upload et /api/documents/{id}

    @PostMapping
    public DocumentMedical create(@RequestBody DocumentMedical documentMedical) {
        return documentMedicalService.create(documentMedical);
    }

    @GetMapping
    public List<DocumentMedical> findAll() {
        return documentMedicalService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<DocumentMedical> findById(@PathVariable Long id) {
        return documentMedicalService.findById(id);
    }

    @PutMapping("/{id}")
    public DocumentMedical update(@PathVariable Long id, @RequestBody DocumentMedical documentMedical) {
        return documentMedicalService.update(id, documentMedical);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        documentMedicalService.delete(id);
    }
}