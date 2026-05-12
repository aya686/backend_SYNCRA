package org.example.backend_pi.service;
// service/ContentModerationService.java
// ══════════════════════════════════════════════════════════════════
// Service Spring Boot qui appelle le microservice Python de modération.
// Appelé automatiquement dans MachineServiceImpl.addMachine()
// et ServiceServiceImpl.addService() AVANT la sauvegarde en base.
// ══════════════════════════════════════════════════════════════════

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ContentModerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentModerationService.class);

    @Value("${moderation.service.url:http://localhost:5001}")
    private String moderationServiceUrl;

    @Value("${moderation.enabled:true}")
    private boolean moderationEnabled;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─── RÉSULTAT DE MODÉRATION ───────────────────────────────────

    public static class ModerationResult {
        /** true = contenu acceptable, false = rejeté automatiquement */
        public final boolean approved;
        /** CLEAN | OFFENSIVE | HATE_SPEECH */
        public final String verdict;
        /** Score de toxicité 0.0 → 1.0 */
        public final double score;
        /** Message lisible pour l'utilisateur (null si CLEAN) */
        public final String reason;
        /** Mots détectés comme problématiques */
        public final List<String> blockedWords;

        public ModerationResult(boolean approved, String verdict,
                                double score, String reason, List<String> blockedWords) {
            this.approved     = approved;
            this.verdict      = verdict;
            this.score        = score;
            this.reason       = reason;
            this.blockedWords = blockedWords != null ? blockedWords : Collections.emptyList();
        }

        /** Résultat approuvé par défaut (utilisé si le service ML est indisponible) */
        public static ModerationResult approved() {
            return new ModerationResult(true, "CLEAN", 0.0, null, Collections.emptyList());
        }

        /** Résultat de rejet automatique */
        public static ModerationResult rejected(String verdict, double score,
                                                String reason, List<String> words) {
            return new ModerationResult(false, verdict, score, reason, words);
        }
    }

    // ─── MÉTHODE PRINCIPALE ──────────────────────────────────────

    /**
     * Analyse le contenu d'une machine ou d'un service avant sauvegarde.
     *
     * @param texts      Liste des champs texte à analyser (nom + description)
     * @param entityType "MACHINE" ou "SERVICE"
     * @param entityId   ID de l'entité (pour le logging)
     * @return ModerationResult avec approved=true si le contenu est acceptable
     */
    public ModerationResult moderate(List<String> texts, String entityType, Long entityId) {

        // Si la modération est désactivée dans application.properties
        if (!moderationEnabled) {
            log.info("[Modération] Désactivée — contenu approuvé par défaut");
            return ModerationResult.approved();
        }

        // Filtrer les textes vides
        List<String> validTexts = texts.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());

        if (validTexts.isEmpty()) {
            return ModerationResult.approved();
        }

        try {
            // ── Construire la requête vers le microservice Python ──
            Map<String, Object> payload = new HashMap<>();
            payload.put("texts",      validTexts);
            payload.put("entityType", entityType);
            if (entityId != null) payload.put("entityId", entityId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    moderationServiceUrl + "/moderate",
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("[Modération] Service ML retourné HTTP {}", response.getStatusCode());
                return ModerationResult.approved(); // fail-open : ne pas bloquer si ML échoue
            }

            // ── Parser la réponse ──────────────────────────────────
            JsonNode root = objectMapper.readTree(response.getBody());

            boolean approved = root.path("approved").asBoolean(true);
            String verdict   = root.path("verdict").asText("CLEAN");
            double score     = root.path("score").asDouble(0.0);
            String reason    = root.path("reason").asText(null);

            List<String> blocked = new ArrayList<>();
            JsonNode details = root.path("details");
            if (details.isArray()) {
                for (JsonNode d : details) {
                    JsonNode bw = d.path("blocked_words");
                    if (bw.isArray()) {
                        bw.forEach(w -> blocked.add(w.asText()));
                    }
                }
            }

            // ── Adapter le message en français ────────────────────
            String userMessage = buildUserMessage(verdict, blocked, entityType);

            log.info("[Modération] {} #{} → {} (score:{}, mots:{})",
                    entityType, entityId, verdict, score, blocked);

            if (approved) {
                return ModerationResult.approved();
            } else {
                return ModerationResult.rejected(verdict, score, userMessage, blocked);
            }

        } catch (ResourceAccessException e) {
            // Le service Python n'est pas démarré → fail-open (ne pas bloquer)
            log.warn("[Modération] Service ML indisponible ({}). Contenu approuvé par défaut.", e.getMessage());
            return ModerationResult.approved();
        } catch (Exception e) {
            log.error("[Modération] Erreur inattendue", e);
            return ModerationResult.approved(); // fail-open
        }
    }

    // ─── SURCHARGE PRATIQUE ───────────────────────────────────────

    /** Analyse un nom + une description */
    public ModerationResult moderate(String name, String description,
                                     String entityType, Long entityId) {
        List<String> texts = new ArrayList<>();
        if (name        != null) texts.add(name);
        if (description != null) texts.add(description);
        return moderate(texts, entityType, entityId);
    }

    // ─── MESSAGES UTILISATEUR LOCALISÉS ──────────────────────────

    private String buildUserMessage(String verdict, List<String> blockedWords, String entityType) {
        String entityFr = "SERVICE".equalsIgnoreCase(entityType) ? "service" : "machine";

        switch (verdict) {
            case "HATE_SPEECH":
                return String.format(
                        "Votre %s a été automatiquement rejeté(e) car son contenu contient " +
                                "des propos haineux ou discriminatoires. " +
                                "Veuillez reformuler votre description de manière professionnelle.%s",
                        entityFr,
                        blockedWords.isEmpty() ? "" : " Termes détectés : " + String.join(", ", blockedWords) + "."
                );

            case "OFFENSIVE":
                return String.format(
                        "Votre %s a été automatiquement rejeté(e) car son contenu contient " +
                                "des termes offensants ou inappropriés. " +
                                "Veuillez reformuler votre description de manière professionnelle.%s",
                        entityFr,
                        blockedWords.isEmpty() ? "" : " Termes détectés : " + String.join(", ", blockedWords) + "."
                );

            default:
                return String.format(
                        "Votre %s n'a pas été publié(e) car son contenu ne respecte pas " +
                                "nos règles de bonne conduite.", entityFr
                );
        }
    }
}