package com.synchub.ms6.controller;

import com.synchub.ms6.model.ChatbotResponse;
import com.synchub.ms6.service.NLPChatbotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour le Chatbot NLP
 * Endpoints pour tester le NLP via Postman et intégration Angular
 * Algorithmes: TF-IDF, Similarité Cosinus, NER, Sentiment Analysis
 */
@RestController
@RequestMapping("/ms6/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Pour permettre les requêtes depuis Angular
@Slf4j
public class ChatbotController {

    private final NLPChatbotService chatbotService;

    /**
     * Endpoint principal: Envoyer un message au chatbot
     * POST /api/chatbot/message
     * Traite un message utilisateur avec NLP et retourne une réponse intelligente
     */
    @PostMapping("/message")
    public ResponseEntity<ChatbotResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("📩 Message reçu de l'utilisateur {}: '{}'", request.getUserId(), request.getMessage());
        
        // Traiter le message avec le service NLP
        ChatbotResponse response = chatbotService.processMessage(
            request.getMessage(), 
            request.getUserId()
        );
        
        // Ajouter les métadonnées
        response.setOriginalMessage(request.getMessage());
        response.setUserId(request.getUserId());
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        response.setSentimentLabel(response.getSentimentLabel());
        response.setIntentDescription(response.getIntent().getDescription());
        
        log.info("✅ Réponse générée en {}ms - Intention: {} (confiance: {:.1f}%)",
            response.getProcessingTimeMs(),
            response.getIntent(),
            response.getConfidence() * 100
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint GET simplifié pour tests rapides
     * GET /api/chatbot/ask?message=bonjour&userId=1
     * Endpoint GET simplifié pour tester rapidement le chatbot
     */
    @GetMapping("/ask")
    public ResponseEntity<ChatbotResponse> askGet(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "1") Long userId) {
        
        ChatMessageRequest request = new ChatMessageRequest();
        request.setMessage(message);
        request.setUserId(userId);
        
        return sendMessage(request);
    }

    /**
     * Endpoint pour obtenir les informations sur le modèle NLP
     * GET /api/chatbot/nlp-info
     * Retourne les statistiques du modèle NLP (taille vocabulaire, intentions supportées)
     */
    @GetMapping("/nlp-info")
    public ResponseEntity<Map<String, Object>> getNLPInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("algorithm", "TF-IDF + Cosine Similarity");
        info.put("version", "1.0.0");
        info.put("intentions", List.of(
            Map.of("name", "SEARCH_PRODUCT", "description", "Recherche de produits", "examples", List.of("je cherche un iphone", "trouver produit")),
            Map.of("name", "CHECK_PRICE", "description", "Vérification de prix", "examples", List.of("quel est le prix", "combien ça coûte")),
            Map.of("name", "TRACK_ORDER", "description", "Suivi de commande", "examples", List.of("où est ma commande", "suivi commande")),
            Map.of("name", "GET_RECOMMENDATION", "description", "Recommandations", "examples", List.of("que me recommandes-tu", "suggestions")),
            Map.of("name", "CHECK_STOCK", "description", "Vérification stock", "examples", List.of("en stock", "disponible")),
            Map.of("name", "APPLY_PROMO", "description", "Codes promo", "examples", List.of("code promo", "réduction")),
            Map.of("name", "COMPLAINT", "description", "Réclamation", "examples", List.of("problème", "je ne suis pas satisfait")),
            Map.of("name", "GREETING", "description", "Salutation", "examples", List.of("bonjour", "salut", "hello")),
            Map.of("name", "THANKS", "description", "Remerciement", "examples", List.of("merci", "thanks")),
            Map.of("name", "GOODBYE", "description", "Au revoir", "examples", List.of("au revoir", "bye")),
            Map.of("name", "HELP", "description", "Aide", "examples", List.of("aide", "help", "que peux-tu faire"))
        ));
        info.put("features", List.of(
            "TF-IDF Vectorization",
            "Cosine Similarity Classification",
            "Named Entity Recognition (NER)",
            "Sentiment Analysis",
            "Stemming (français)",
            "Negation Handling"
        ));
        info.put("supportedLanguages", List.of("français", "anglais (basique)"));
        
        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint pour tester plusieurs messages à la fois (batch test)
     * POST /api/chatbot/test-batch
     * Envoie plusieurs messages pour tester la couverture des intentions
     */
    @PostMapping("/test-batch")
    public ResponseEntity<List<ChatbotResponse>> testBatch(@RequestBody BatchTestRequest request) {
        List<ChatbotResponse> responses = request.getMessages().stream()
            .map(msg -> {
                ChatMessageRequest req = new ChatMessageRequest();
                req.setMessage(msg);
                req.setUserId(request.getUserId());
                return chatbotService.processMessage(msg, request.getUserId());
            })
            .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint de santé pour vérifier que le service est opérationnel
     * GET /api/chatbot/health
     * Endpoint de healthcheck pour le monitoring
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "chatbot-nlp");
        health.put("algorithm", "tfidf-cosine");
        return ResponseEntity.ok(health);
    }

    // ==================== Classes de requête ====================

    @Data
    public static class ChatMessageRequest {
        private String message;
        private Long userId = 1L; // Default user
    }

    @Data
    public static class BatchTestRequest {
        private List<String> messages;
        private Long userId = 1L;
    }
}
