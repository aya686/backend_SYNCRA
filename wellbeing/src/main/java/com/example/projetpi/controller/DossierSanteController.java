package com.example.projetpi.controller;

import com.example.projetpi.entity.Antecedent;
import com.example.projetpi.entity.DocumentMedical;
import com.example.projetpi.entity.DossierSante;
import com.example.projetpi.repository.AntecedentRepository;
import com.example.projetpi.repository.DocumentMedicalRepository;
import com.example.projetpi.repository.DossierSanteRepository;
import com.example.projetpi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class DossierSanteController {

    @Autowired
    private DossierSanteRepository dossierSanteRepository;

    @Autowired
    private AntecedentRepository antecedentRepository;

    @Autowired
    private DocumentMedicalRepository documentMedicalRepository;

    // Répertoire de stockage des fichiers uploadés
    private static final String UPLOAD_DIR = "uploads/documents/";

    // ── GET: Dossier par utilisateur ──────────────────────────────────
    @GetMapping("/dossier-sante/utilisateur/{userId}")
    public ResponseEntity<DossierSante> getDossierByUtilisateur(@PathVariable Long userId) {
        Optional<DossierSante> dossier = dossierSanteRepository.findByUtilisateurId(userId);
        return dossier.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET: Dossier par ID ────────────────────────────────────────────
    @GetMapping("/dossier-sante/{id}")
    public ResponseEntity<DossierSante> getDossierById(@PathVariable Long id) {
        return dossierSanteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT: Mettre à jour les infos du dossier ────────────────────────
    @PutMapping("/dossier-sante/{id}")
    public ResponseEntity<DossierSante> updateDossier(
            @PathVariable Long id,
            @RequestBody DossierSante updated) {

        return dossierSanteRepository.findById(id).map(existing -> {
            existing.setGroupeSanguin(updated.getGroupeSanguin());
            existing.setGenre(updated.getGenre());
            return ResponseEntity.ok(dossierSanteRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── POST: Créer un dossier pour un utilisateur ────────────────────
    @PostMapping("/dossier-sante")
    public ResponseEntity<DossierSante> createDossier(@RequestBody DossierSante dossier) {
        // Vérifier qu'il n'existe pas déjà
        Optional<DossierSante> existing = dossierSanteRepository.findByUtilisateurId(dossier.getUtilisateurId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        dossier.setDateCreation(java.time.LocalDate.now());
        return ResponseEntity.ok(dossierSanteRepository.save(dossier));
    }

    // ── POST: Ajouter un antécédent au dossier ────────────────────────
    @PostMapping("/antecedents/dossier/{dossierId}")
    public ResponseEntity<Antecedent> addAntecedent(
            @PathVariable Long dossierId,
            @RequestBody Antecedent antecedent) {

        return dossierSanteRepository.findById(dossierId).map(dossier -> {
            antecedent.setDossierSante(dossier);
            return ResponseEntity.ok(antecedentRepository.save(antecedent));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE: Supprimer un antécédent ───────────────────────────────
    @DeleteMapping("/antecedents/{id}")
    public ResponseEntity<Void> deleteAntecedent(@PathVariable Long id) {
        if (!antecedentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        antecedentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── POST: Upload document médical ─────────────────────────────────
    @PostMapping("/documents/upload")
    public ResponseEntity<DocumentMedical> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dossierId") Long dossierId,
            @RequestParam("nom") String nom,
            @RequestParam("type") String type) {

        Optional<DossierSante> dossierOpt = dossierSanteRepository.findById(dossierId);
        if (dossierOpt.isEmpty()) return ResponseEntity.notFound().build();

        try {
            // Créer le répertoire si nécessaire
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String extension = "";
            int dotIndex = file.getOriginalFilename() != null ? file.getOriginalFilename().lastIndexOf('.') : -1;
            if (dotIndex > 0) extension = file.getOriginalFilename().substring(dotIndex);

            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Sauvegarder en base
            DocumentMedical doc = new DocumentMedical();
            doc.setNom(nom);
            doc.setType(type);
            doc.setUrl("/uploads/documents/" + filename);
            doc.setDossierSante(dossierOpt.get());

            return ResponseEntity.ok(documentMedicalRepository.save(doc));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── DELETE: Supprimer un document ─────────────────────────────────
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Optional<DocumentMedical> docOpt = documentMedicalRepository.findById(id);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Supprimer le fichier physique
        DocumentMedical doc = docOpt.get();
        if (doc.getUrl() != null) {
            try {
                Path filePath = Paths.get(doc.getUrl().replaceFirst("/", ""));
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
        }

        documentMedicalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}