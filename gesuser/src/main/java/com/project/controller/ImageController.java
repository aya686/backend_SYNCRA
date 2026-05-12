package com.project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Value("${image.upload.directory:./uploads/}")
    private String uploadDirectory;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== UPLOAD IMAGE ===");
            System.out.println("Fichier reçu: " + file.getOriginalFilename());
            System.out.println("Taille: " + file.getSize() + " bytes");

            // Créer le dossier si inexistant
            File directory = new File(uploadDirectory);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("Dossier créé: " + created + " - " + directory.getAbsolutePath());
            }

            // Générer un nom unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Sauvegarder le fichier
            Path path = Paths.get(uploadDirectory + fileName);
            Files.write(path, file.getBytes());

            System.out.println("Fichier sauvegardé: " + path.toAbsolutePath());

            // URL publique
            String imageUrl = "/uploads/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Upload réussi");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Erreur upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur lors de l'upload"));
        }
    }

    // ✅ AJOUTER UN ENDPOINT POUR SERVIR LES IMAGES
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDirectory).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}