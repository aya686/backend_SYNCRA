package tn.esprit.ms2.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms2.entities.StatutTache;
import tn.esprit.ms2.services.TacheService;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final TacheService tacheService;

    @PostMapping("/github")
    public ResponseEntity<String> githubPush(@RequestBody Map<String, Object> payload) {
        try {
            // Extraire les commits
            var commits = (java.util.List<?>) payload.get("commits");
            if (commits == null) return ResponseEntity.ok("no commits");

            Pattern pattern = Pattern.compile("#TASK-(\\d+)\\s+(done|close|fix|terminé)",
                    Pattern.CASE_INSENSITIVE);

            for (Object commitObj : commits) {
                var commit = (Map<?, ?>) commitObj;
                String message = (String) commit.get("message");
                if (message == null) continue;

                Matcher matcher = pattern.matcher(message);
                while (matcher.find()) {
                    Long tacheId = Long.parseLong(matcher.group(1));
                    tacheService.changerStatut(tacheId, StatutTache.TERMINE);
                }
            }
            return ResponseEntity.ok("processed");
        } catch (Exception e) {
            return ResponseEntity.ok("error: " + e.getMessage());
        }
    }
}