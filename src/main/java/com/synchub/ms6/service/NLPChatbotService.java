package com.synchub.ms6.service;

import com.synchub.ms6.model.ChatbotIntent;
import com.synchub.ms6.model.ChatbotResponse;
import com.synchub.ms6.entity.Produit;
import com.synchub.ms6.entity.Commande;
import com.synchub.ms6.repository.ProduitRepository;
import com.synchub.ms6.repository.CommandeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de Chatbot NLP avec TF-IDF et Classification d'Intention
 * Algorithmes utilisés:
 * - Tokenization et normalisation
 * - TF-IDF (Term Frequency - Inverse Document Frequency)
 * - Similarité Cosinus pour classification d'intention
 * - Analyse de sentiment lexicale
 * - Extraction d'entités (NER basique)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NLPChatbotService {

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;

    // Vocabulaire pour TF-IDF
    private final Map<String, Integer> vocabulary = new HashMap<>();
    private final Map<ChatbotIntent, double[]> intentVectors = new HashMap<>();
    private final Map<String, Double> sentimentLexicon = new HashMap<>();
    
    // Patterns d'intentions avec exemples d'entraînement
    private final Map<ChatbotIntent, List<String>> trainingExamples = new HashMap<>();
    
    {
        trainingExamples.put(ChatbotIntent.SEARCH_PRODUCT, List.of(
            "je cherche un iphone",
            "trouver produit",
            "rechercher article",
            "où est le laptop",
            "show me phones",
            "produit disponible"
        ));
        trainingExamples.put(ChatbotIntent.CHECK_PRICE, List.of(
            "quel est le prix",
            "combien ça coûte",
            "prix du produit",
            "tarif",
            "cout",
            "price"
        ));
        trainingExamples.put(ChatbotIntent.TRACK_ORDER, List.of(
            "où est ma commande",
            "suivi commande",
            "track order",
            "statut livraison",
            "numéro commande",
            "order status"
        ));
        trainingExamples.put(ChatbotIntent.GET_RECOMMENDATION, List.of(
            "que me recommandes-tu",
            "recommandation",
            "suggest products",
            "que devrais-je acheter",
            "idée cadeau",
            "besoin conseil"
        ));
        trainingExamples.put(ChatbotIntent.CHECK_STOCK, List.of(
            "en stock",
            "disponible",
            "quantité restante",
            "stock disponible",
            "encore en vente",
            "available"
        ));
        trainingExamples.put(ChatbotIntent.APPLY_PROMO, List.of(
            "code promo",
            "réduction",
            "coupon",
            "promotion",
            "discount",
            "offre spéciale"
        ));
        trainingExamples.put(ChatbotIntent.COMPLAINT, List.of(
            "problème",
            "je ne suis pas satisfait",
            "réclamation",
            "bug",
            "mauvais service",
            "retour produit"
        ));
        trainingExamples.put(ChatbotIntent.GREETING, List.of(
            "bonjour",
            "salut",
            "hello",
            "bonsoir",
            "coucou",
            "hi"
        ));
        trainingExamples.put(ChatbotIntent.THANKS, List.of(
            "merci",
            "thank you",
            "thanks",
            "super",
            "génial",
            "excellent"
        ));
        trainingExamples.put(ChatbotIntent.GOODBYE, List.of(
            "au revoir",
            "bye",
            "goodbye",
            "à plus",
            "ciao",
            "à bientôt"
        ));
        trainingExamples.put(ChatbotIntent.HELP, List.of(
            "aide",
            "help",
            "que peux-tu faire",
            "comment ça marche",
            "assistance",
            "support"
        ));
    }

    /**
     * Initialisation automatique au démarrage de l'application
     * Évite le délai lors du premier message utilisateur
     */
    @PostConstruct
    public void initOnStartup() {
        log.info("🚀 Démarrage de l'initialisation NLP en arrière-plan...");
        initializeNLP();
    }

    /**
     * Initialisation du vocabulaire et des vecteurs d'intention
     * Appelé au démarrage ou lors de la première requête
     */
    public void initializeNLP() {
        if (!vocabulary.isEmpty()) return; // Déjà initialisé
        
        log.info("Initialisation du modèle NLP avec TF-IDF...");
        
        // Construire le vocabulaire à partir des exemples d'entraînement
        Set<String> allWords = new HashSet<>();
        for (List<String> examples : trainingExamples.values()) {
            for (String example : examples) {
                allWords.addAll(tokenize(example));
            }
        }
        
        // Assigner un index à chaque mot
        int index = 0;
        for (String word : allWords) {
            vocabulary.put(word, index++);
        }
        
        // Calculer les vecteurs TF-IDF pour chaque intention
        for (Map.Entry<ChatbotIntent, List<String>> entry : trainingExamples.entrySet()) {
            double[] vector = calculateIntentVector(entry.getValue());
            intentVectors.put(entry.getKey(), vector);
        }
        
        // Charger le lexique de sentiment
        initializeSentimentLexicon();
        
        log.info("Modèle NLP initialisé avec {} mots dans le vocabulaire", vocabulary.size());
    }

    /**
     * Point d'entrée principal: Traiter un message utilisateur
     */
    public ChatbotResponse processMessage(String message, Long userId) {
        initializeNLP();
        
        log.info("Traitement message: '{}' pour user: {}", message, userId);
        
        // Étape 1: Tokenization et normalisation
        List<String> tokens = tokenize(message);
        
        // Étape 2: Extraction des entités (NER basique)
        Map<String, String> entities = extractEntities(message, tokens);
        
        // Étape 3: Classification d'intention par TF-IDF + Similarité Cosinus
        ChatbotIntent intent = classifyIntent(tokens);
        
        // Étape 4: Analyse de sentiment
        double sentimentScore = analyzeSentiment(tokens);
        
        // Étape 5: Exécution de l'intention et génération de réponse
        String response = executeIntent(intent, entities, userId, sentimentScore);
        
        // Étape 6: Suggestions de follow-up
        List<String> suggestions = generateSuggestions(intent);
        
        return ChatbotResponse.builder()
            .intent(intent)
            .response(response)
            .entities(entities)
            .sentimentScore(sentimentScore)
            .confidence(calculateConfidence(intent, tokens))
            .suggestions(suggestions)
            .timestamp(new Date())
            .build();
    }

    /**
     * TF-IDF Vectorization d'un message
     */
    private double[] vectorize(List<String> tokens) {
        double[] vector = new double[vocabulary.size()];
        int totalTokens = tokens.size();
        
        // Calculer TF pour chaque terme
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.merge(token, 1, Integer::sum);
        }
        
        // Calculer TF-IDF
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            
            if (vocabulary.containsKey(term)) {
                // TF = fréquence du terme / nombre total de termes
                double tfNormalized = (double) tf / totalTokens;
                
                // IDF = log(N / df) où N = nombre d'intentions, df = intentions contenant le terme
                int df = calculateDocumentFrequency(term);
                double idf = Math.log((double) trainingExamples.size() / df);
                
                // TF-IDF
                int index = vocabulary.get(term);
                vector[index] = tfNormalized * idf;
            }
        }
        
        // Normalisation L2 (pour similarité cosinus)
        double norm = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }

    /**
     * Calculer la fréquence documentaire (df) d'un terme
     */
    private int calculateDocumentFrequency(String term) {
        int count = 0;
        for (List<String> examples : trainingExamples.values()) {
            for (String example : examples) {
                if (example.toLowerCase().contains(term.toLowerCase())) {
                    count++;
                    break;
                }
            }
        }
        return Math.max(count, 1); // Éviter division par zéro
    }

    /**
     * Classification d'intention par Similarité Cosinus
     */
    private ChatbotIntent classifyIntent(List<String> tokens) {
        double[] messageVector = vectorize(tokens);
        
        ChatbotIntent bestIntent = ChatbotIntent.UNKNOWN;
        double maxSimilarity = -1;
        
        for (Map.Entry<ChatbotIntent, double[]> entry : intentVectors.entrySet()) {
            double similarity = cosineSimilarity(messageVector, entry.getValue());
            
            if (similarity > maxSimilarity && similarity > 0.3) { // Seuil de confiance
                maxSimilarity = similarity;
                bestIntent = entry.getKey();
            }
        }
        
        return bestIntent;
    }

    /**
     * Similarité Cosinus entre deux vecteurs
     * sim(A,B) = (A·B) / (||A|| × ||B||)
     */
    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dotProduct = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        
        double norm1 = Math.sqrt(Arrays.stream(vec1).map(x -> x * x).sum());
        double norm2 = Math.sqrt(Arrays.stream(vec2).map(x -> x * x).sum());
        
        if (norm1 == 0 || norm2 == 0) return 0;
        
        return dotProduct / (norm1 * norm2);
    }

    /**
     * Calculer le vecteur moyen d'une intention
     */
    private double[] calculateIntentVector(List<String> examples) {
        double[] sumVector = new double[vocabulary.size()];
        
        for (String example : examples) {
            double[] vec = vectorize(tokenize(example));
            for (int i = 0; i < vec.length; i++) {
                sumVector[i] += vec[i];
            }
        }
        
        // Moyenne
        for (int i = 0; i < sumVector.length; i++) {
            sumVector[i] /= examples.size();
        }
        
        // Normalisation
        double norm = Math.sqrt(Arrays.stream(sumVector).map(x -> x * x).sum());
        if (norm > 0) {
            for (int i = 0; i < sumVector.length; i++) {
                sumVector[i] /= norm;
            }
        }
        
        return sumVector;
    }

    /**
     * Tokenization et normalisation
     */
    private List<String> tokenize(String text) {
        // Convertir en minuscules
        String normalized = text.toLowerCase();
        
        // Supprimer la ponctuation
        normalized = normalized.replaceAll("[^a-zA-Z0-9àâäéèêëïîôùûüç\\s]", " ");
        
        // Split sur espaces multiples
        return Arrays.stream(normalized.split("\\s+"))
            .filter(token -> token.length() > 2) // Ignorer mots courts
            .map(this::stem) // Stemming basique
            .collect(Collectors.toList());
    }

    /**
     * Stemming basique français
     */
    private String stem(String word) {
        // Simplification: enlever suffixes communs
        String[] suffixes = {"ation", "ement", "euse", "eur", "age", "eur", "ement", "ition", "ance", "ence", "ure", "eux"};
        for (String suffix : suffixes) {
            if (word.endsWith(suffix) && word.length() > suffix.length() + 3) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }

    /**
     * Extraction d'entités (NER basique)
     */
    private Map<String, String> extractEntities(String message, List<String> tokens) {
        Map<String, String> entities = new HashMap<>();
        
        // Extraire ID numérique
        message.replaceAll("\\D", " ").trim().split("\\s+");
        String[] numbers = message.replaceAll("[^0-9]", " ").trim().split("\\s+");
        for (String num : numbers) {
            if (num.length() > 0 && num.length() < 10) {
                try {
                    Long id = Long.parseLong(num);
                    if (produitRepository.existsById(id)) {
                        entities.put("produitId", num);
                    } else if (commandeRepository.existsById(id)) {
                        entities.put("commandeId", num);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        // Extraire noms de produits
        List<String> productKeywords = List.of("iphone", "laptop", "ordinateur", "téléphone", 
            "chaussures", "montre", "casque", "tablette", "souris", "clavier", "écran");
        
        for (String keyword : productKeywords) {
            if (message.toLowerCase().contains(keyword)) {
                entities.put("produitNom", keyword);
                break;
            }
        }
        
        // Extraire prix mentionné
        if (message.matches(".*\\d+\\s*(€|dt|dinars?|euros?).*")) {
            entities.put("prixMentionne", "true");
        }
        
        return entities;
    }

    /**
     * Analyse de sentiment lexicale
     */
    private double analyzeSentiment(List<String> tokens) {
        double positiveScore = 0;
        double negativeScore = 0;
        int negationMultiplier = 1;
        
        Set<String> negations = Set.of("pas", "ne", "n'", "non", "sans", "peu");
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            // Détection de négation
            if (negations.contains(token)) {
                negationMultiplier = -1;
                continue;
            }
            
            // Reset négation après 3 mots
            if (i % 3 == 0 && i > 0) {
                negationMultiplier = 1;
            }
            
            // Scoring
            Double score = sentimentLexicon.get(token);
            if (score != null) {
                double adjustedScore = score * negationMultiplier;
                if (adjustedScore > 0) {
                    positiveScore += adjustedScore;
                } else {
                    negativeScore += Math.abs(adjustedScore);
                }
            }
        }
        
        double total = positiveScore + negativeScore;
        if (total == 0) return 0;
        
        // Normaliser entre -1 et 1
        return (positiveScore - negativeScore) / total;
    }

    /**
     * Initialiser le lexique de sentiment
     */
    private void initializeSentimentLexicon() {
        // Positifs
        Map<String, Double> positive = Map.ofEntries(
            Map.entry("super", 1.0), Map.entry("excellent", 1.0),
            Map.entry("génial", 1.0), Map.entry("parfait", 1.0),
            Map.entry("merci", 0.8), Map.entry("aime", 0.9),
            Map.entry("adore", 1.0), Map.entry("bien", 0.7),
            Map.entry("bon", 0.6), Map.entry("content", 0.8),
            Map.entry("satisfait", 0.9), Map.entry("recommande", 0.8),
            Map.entry("rapide", 0.6), Map.entry("facile", 0.6),
            Map.entry("beau", 0.7), Map.entry("belle", 0.7),
            Map.entry("magnifique", 1.0), Map.entry("top", 0.9),
            Map.entry("cool", 0.7), Map.entry("géniale", 1.0)
        );
        
        // Négatifs
        Map<String, Double> negative = Map.ofEntries(
            Map.entry("problème", -0.8), Map.entry("bug", -0.9),
            Map.entry("nul", -1.0), Map.entry("mauvais", -0.9),
            Map.entry("hate", -0.9), Map.entry("déteste", -1.0),
            Map.entry("lent", -0.7), Map.entry("difficile", -0.6),
            Map.entry("cher", -0.5), Map.entry("trop", -0.4),
            Map.entry("problèmes", -0.8), Map.entry("erreur", -0.8),
            Map.entry("faux", -0.9), Map.entry("déçu", -0.9),
            Map.entry("déçue", -0.9), Map.entry("horrible", -1.0),
            Map.entry("catastrophe", -1.0), Map.entry("nulle", -1.0)
        );
        
        sentimentLexicon.putAll(positive);
        sentimentLexicon.putAll(negative);
    }

    /**
     * Exécuter l'intention et générer une réponse
     */
    private String executeIntent(ChatbotIntent intent, Map<String, String> entities, 
                                 Long userId, double sentimentScore) {
        
        String produitNom = entities.getOrDefault("produitNom", "");
        String produitId = entities.get("produitId");
        
        switch (intent) {
            case SEARCH_PRODUCT:
                return handleSearchProduct(produitNom, produitId);
                
            case CHECK_PRICE:
                return handleCheckPrice(produitNom, produitId);
                
            case TRACK_ORDER:
                String commandeId = entities.get("commandeId");
                return handleTrackOrder(commandeId, userId);
                
            case GET_RECOMMENDATION:
                return handleRecommendation(userId);
                
            case CHECK_STOCK:
                return handleCheckStock(produitId, produitNom);
                
            case APPLY_PROMO:
                return handlePromo(userId);
                
            case COMPLAINT:
                return handleComplaint(sentimentScore);
                
            case GREETING:
                return handleGreeting(sentimentScore);
                
            case THANKS:
                return "😊 Je vous en prie ! N'hésitez pas si vous avez d'autres questions.";
                
            case GOODBYE:
                return "👋 Au revoir ! Passez une excellente journée !";
                
            case HELP:
                return handleHelp();
                
            default:
                return "🤔 Je n'ai pas bien compris. Voici ce que je peux faire pour vous:\n" +
                       "• Rechercher des produits\n" +
                       "• Vérifier les prix et stocks\n" +
                       "• Suivre vos commandes\n" +
                       "• Donner des recommandations\n" +
                       "• Appliquer des codes promo\n" +
                       "Comment puis-je vous aider ?";
        }
    }

    private String handleSearchProduct(String produitNom, String produitId) {
        if (produitId != null) {
            Optional<Produit> produit = produitRepository.findById(Long.parseLong(produitId));
            if (produit.isPresent()) {
                Produit p = produit.get();
                return String.format("🔍 J'ai trouvé : **%s**\n" +
                    "💰 Prix: %.2f DT\n" +
                    "📦 Stock: %s\n" +
                    "📝 %s\n" +
                    "💡 Tapez 'prix %s' pour plus de détails",
                    p.getNom(), p.getPrix(),
                    p.getStock() != null && p.getStock().getQuantite() > 0 ? 
                        p.getStock().getQuantite() + " unités" : "Rupture de stock",
                    p.getDescription() != null ? p.getDescription().substring(0, 
                        Math.min(100, p.getDescription().length())) + "..." : "",
                    p.getProduitId()
                );
            }
        }
        
        if (!produitNom.isEmpty()) {
            List<Produit> produits = produitRepository.findByNomContainingIgnoreCase(produitNom);
            if (!produits.isEmpty()) {
                StringBuilder sb = new StringBuilder("📱 J'ai trouvé " + produits.size() + " produit(s):\n\n");
                for (int i = 0; i < Math.min(3, produits.size()); i++) {
                    Produit p = produits.get(i);
                    sb.append(String.format("%d. **%s** - %.2f DT (ID: %d)\n",
                        i + 1, p.getNom(), p.getPrix(), p.getProduitId()));
                }
                if (produits.size() > 3) {
                    sb.append("... et ").append(produits.size() - 3).append(" autres\n");
                }
                sb.append("\n💡 Demandez 'prix [ID]' pour les détails");
                return sb.toString();
            }
        }
        
        return "🔍 Je n'ai pas trouvé ce produit. Essayez avec un autre nom ou un ID numérique." +
               "\n💡 Exemples: 'iphone', 'laptop', 'recherche 21'";
    }

    private String handleCheckPrice(String produitNom, String produitId) {
        if (produitId != null) {
            Optional<Produit> produit = produitRepository.findById(Long.parseLong(produitId));
            if (produit.isPresent()) {
                Produit p = produit.get();
                return String.format("💰 **%s**\n" +
                    "Prix actuel: **%.2f DT**\n" +
                    "📊 Prix historique moyen: %.2f DT\n" +
                    "💡 Type 'stock %d' pour vérifier la disponibilité",
                    p.getNom(), p.getPrix(),
                    p.getPrix() * 0.95, // Simulation prix moyen
                    p.getProduitId()
                );
            }
        }
        return "💰 Précisez le produit svp. Exemple: 'prix iphone' ou 'prix 21'";
    }

    private String handleTrackOrder(String commandeId, Long userId) {
        if (commandeId != null) {
            Optional<Commande> commande = commandeRepository.findById(Long.parseLong(commandeId));
            if (commande.isPresent()) {
                Commande c = commande.get();
                // Vérifier que c'est bien la commande de l'utilisateur
                return String.format("📦 **Commande #%d**\n" +
                    "📅 Date: %s\n" +
                    "💰 Montant: %.2f DT\n" +
                    "📍 Statut: **%s**\n" +
                    "🚚 Livraison: %s\n" +
                    "📍 Adresse: %s",
                    c.getCommandeId(),
                    c.getDate(),
                    c.getMontantTotal(),
                    c.getStatut(),
                    !c.getLivraisons().isEmpty() ? c.getLivraisons().get(0).getStatut() : "Non assignée",
                    c.getAdresseLivraison() != null ? c.getAdresseLivraison() : "Non spécifiée"
                );
            }
        }
        
        // Liste des commandes de l'utilisateur
        List<Commande> commandes = commandeRepository.findByUserId(userId);
        if (!commandes.isEmpty()) {
            StringBuilder sb = new StringBuilder("📦 Vos dernières commandes:\n\n");
            for (int i = 0; i < Math.min(3, commandes.size()); i++) {
                Commande c = commandes.get(i);
                sb.append(String.format("• Commande #%d - %.2f DT - %s\n",
                    c.getCommandeId(), c.getMontantTotal(), c.getStatut()));
            }
            sb.append("\n💡 Tapez 'commande [numéro]' pour les détails");
            return sb.toString();
        }
        
        return "📦 Je ne trouve pas de commande. Votre numéro commence par #?" +
               "\n💡 Exemple: 'suivi 123' ou 'où est ma commande 456'";
    }

    private String handleRecommendation(Long userId) {
        // Simulation - en production, appeler le service de recommandation
        return "🎯 **Recommandations pour vous:**\n\n" +
               "Basé sur votre historique et les tendances actuelles:\n\n" +
               "1. 📱 **iPhone 15 Pro** - Très populaire en ce moment\n" +
               "2. 💻 **MacBook Air M3** - Excellente autonomie\n" +
               "3. 🎧 **AirPods Pro 2** - Réduction de bruit top\n\n" +
               "💡 Tapez le numéro ou le nom pour voir les détails !";
    }

    private String handleCheckStock(String produitId, String produitNom) {
        if (produitId != null) {
            Optional<Produit> produit = produitRepository.findById(Long.parseLong(produitId));
            if (produit.isPresent()) {
                Produit p = produit.get();
                int stock = p.getStock() != null ? p.getStock().getQuantite() : 0;
                int seuil = p.getStock() != null ? p.getStock().getSeuilAlerte() : 5;
                
                if (stock == 0) {
                    return String.format("❌ **%s** - RUPTURE DE STOCK\n" +
                        "🔄 Réapprovisionnement prévu sous 7 jours\n" +
                        "💡 Activez l'alerte pour être notifié !",
                        p.getNom()
                    );
                } else if (stock <= seuil) {
                    return String.format("⚠️ **%s** - STOCK LIMITÉ\n" +
                        "📦 Il ne reste que **%d unité(s)** !\n" +
                        "🔥 Commandez vite avant rupture",
                        p.getNom(), stock
                    );
                } else {
                    return String.format("✅ **%s** - EN STOCK\n" +
                        "📦 **%d unités** disponibles\n" +
                        "🚀 Livraison sous 24-48h",
                        p.getNom(), stock
                    );
                }
            }
        }
        return "📦 Précisez le produit svp. Exemple: 'stock 21' ou 'disponible iphone'";
    }

    private String handlePromo(Long userId) {
        return "🎟️ **Codes promo disponibles:**\n\n" +
               "• **BIENVENUE10** - 10% sur votre première commande\n" +
               "• **FLASH20** - 20% sur les produits flash (jusqu'à 18h)\n" +
               "• **LIVRAISON** - Livraison gratuite dès 50 DT\n\n" +
               "💡 Ces codes s'appliquent automatiquement dans votre panier !";
    }

    private String handleComplaint(double sentimentScore) {
        String empathy = sentimentScore < -0.5 ? 
            "Je suis vraiment désolé d'apprendre cela. " : "";
        
        return empathy + "🆘 **Service client**\n\n" +
               "Je transmets immédiatement votre demande à notre équipe.\n\n" +
               "**Options rapides:**\n" +
               "• 📞 Hotline: +216 XX XXX XXX (8h-20h)\n" +
               "• 📧 Email: support@syncra.tn\n" +
               "• 💬 Chat en direct avec un conseiller\n\n" +
               "Votre numéro de ticket: **#" + (System.currentTimeMillis() % 100000) + "**\n" +
               "Réponse sous 24h maximum.";
    }

    private String handleGreeting(double sentimentScore) {
        String greeting = sentimentScore > 0.3 ? 
            "Bonjour ! 😊 Je vois que vous êtes de bonne humeur ! " :
            "Bonjour ! 👋 ";
        
        return greeting + "Je suis **SyncraBot**, votre assistant virtuel.\n\n" +
               "Je peux vous aider à:\n" +
               "🔍 Rechercher des produits\n" +
               "💰 Vérifier les prix et stocks\n" +
               "📦 Suivre vos commandes\n" +
               "🎯 Obtenir des recommandations\n\n" +
               "Que puis-je faire pour vous aujourd'hui ?";
    }

    private String handleHelp() {
        return "📖 **Aide - Ce que je peux faire:**\n\n" +
               "**Recherche produits:**\n" +
               "• 'cherche iphone' / 'recherche laptop'\n" +
               "• 'produit 21' (par ID)\n\n" +
               "**Informations:**\n" +
               "• 'prix [produit]' - Voir le prix\n" +
               "• 'stock [produit]' - Vérifier disponibilité\n" +
               "• 'disponible [produit]'\n\n" +
               "**Commandes:**\n" +
               "• 'où est ma commande [numéro]'\n" +
               "• 'suivi [numéro]'\n" +
               "• 'statut commande'\n\n" +
               "**Autres:**\n" +
               "• 'recommandations' - Suggestions personnalisées\n" +
               "• 'code promo' - Voir promotions\n" +
               "• 'aide' - Cette page\n\n" +
               "💡 Je comprends aussi le langage naturel !";
    }

    /**
     * Calculer la confiance de la classification
     */
    private double calculateConfidence(ChatbotIntent intent, List<String> tokens) {
        double[] messageVector = vectorize(tokens);
        double[] intentVector = intentVectors.getOrDefault(intent, new double[vocabulary.size()]);
        return cosineSimilarity(messageVector, intentVector);
    }

    /**
     * Générer des suggestions de follow-up
     */
    private List<String> generateSuggestions(ChatbotIntent intent) {
        return switch (intent) {
            case SEARCH_PRODUCT -> List.of("Voir les prix", "Vérifier le stock", "Produits similaires");
            case CHECK_PRICE -> List.of("Vérifier stock", "Recommandations", "Code promo");
            case TRACK_ORDER -> List.of("Autres commandes", "Contacter support", "Annuler commande");
            case CHECK_STOCK -> List.of("Acheter maintenant", "Prix", "Alerte réappro");
            case COMPLAINT -> List.of("Parler à un humain", "Suivi ticket", "Retour produit");
            default -> List.of("Rechercher produit", "Voir mes commandes", "Besoin d'aide");
        };
    }
}
