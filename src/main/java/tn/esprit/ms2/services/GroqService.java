package tn.esprit.ms2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyserAlerte(String typeAlerte, String messageAlerte,
                                 int niveauCharge, int nbTachesEnRetard,
                                 int nbHeuresSemaine, int nbAlertesActives,
                                 String titresProjetsConcernes) { // ← nouveau param

        String prompt = String.format("""
                        Tu es un assistant de bien-être professionnel pour micro-entrepreneurs tunisiens.
                        
                        Contexte de l'utilisateur :
                        - Type d'alerte : %s
                        - Message d'alerte : %s
                        - Niveau de charge actuel : %d/10
                        - Tâches en retard : %d
                        - Heures travaillées cette semaine : %d
                        - Nombre d'alertes actives non traitées : %d
                        - Projets concernés : %s
                        
                        Génère une analyse SPÉCIFIQUE à cette situation. 
                        Les actionsConcretes doivent être des actions PRÉCISES et ACTIONNABLES 
                        liées aux projets et au type d'alerte mentionnés, pas des conseils génériques.
                        
                        Réponds UNIQUEMENT en JSON valide sans markdown :
                        {
                          "messagePersonnalise": "message direct et empathique en français (2-3 phrases) mentionnant les projets concernés",
                          "scoreRisque": nombre entre 1 et 10,
                          "actionsConcretes": ["action précise 1", "action précise 2", "action précise 3"],
                          "bloquante": true ou false selon si scoreRisque >= 8
                        }
                        """,
                typeAlerte, messageAlerte, niveauCharge,
                nbTachesEnRetard, nbHeuresSemaine, nbAlertesActives,
                titresProjetsConcernes);



        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("temperature", 0.7);

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);
        body.put("messages", List.of(msg));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<?> choices = (List<?>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("[GROQ ERROR] " + e.getMessage());
        }

        return """
            {
              "messagePersonnalise": "Nous avons détecté une surcharge importante. Prenez une pause.",
              "scoreRisque": 7,
              "actionsConcretes": ["Faites une pause de 30 minutes", "Reportez une tâche non urgente", "Contactez votre moniteur"],
              "bloquante": false
            }
            """;
    }
}