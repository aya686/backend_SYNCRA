package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.pifirst.entity.*;
import tn.esprit.pifirst.enums.Statut;
import tn.esprit.pifirst.event.BlockedAccountEvent;
import tn.esprit.pifirst.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private LoginHistoryRepository loginHistoryRepository;

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // ============================================================
    // SSE - NOTIFICATIONS TEMPS RÉEL
    // ============================================================
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        System.out.println("📡 SSE connecté - Total: " + emitters.size());
        return emitter;
    }

    @EventListener
    public void handleBlockedAccount(BlockedAccountEvent event) {
        User user = event.getUser();
        var apiResponse = event.getApiResponse();

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ACCOUNT_BLOCKED");
        notification.put("userId", user.getId());
        notification.put("email", user.getEmail());

        // ✅ Gestion du cas où apiResponse est null (brute force)
        if (apiResponse != null) {
            notification.put("reason", apiResponse.getExplanationText());
            notification.put("riskScore", apiResponse.getRiskScore());
            notification.put("isolationScore", apiResponse.getIsolationScore());
            notification.put("randomForestScore", apiResponse.getRandomForestScore());
            notification.put("ruleTriggered", apiResponse.getRuleTriggered());
        } else {
            notification.put("reason", "Trop de tentatives échouées (brute force)");
            notification.put("riskScore", 95);
            notification.put("isolationScore", 0);
            notification.put("randomForestScore", 0);
            notification.put("ruleTriggered", "brute_force");
        }

        notification.put("timestamp", LocalDateTime.now());

        sendToAllEmitters("blocked", notification);
    }

    private void sendToAllEmitters(String eventName, Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    // ============================================================
    // STATISTIQUES
    // ============================================================
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        long connexionsToday = loginHistoryRepository.countByLoginTimeAfter(today);
        long twoFACount = loginHistoryRepository.countByTwoFactorRequiredTrueAndLoginTimeAfter(today);
        long blockedCount = loginHistoryRepository.countByMlDecisionAndLoginTimeAfter("BLOCKED", today);
        Double avgMlScore = loginHistoryRepository.getAverageRiskScoreForMlDecisions();
        long suspendedCount = userRepository.countByStatut(Statut.SUSPENDU);

        Map<String, Object> stats = new HashMap<>();
        stats.put("connexionsToday", connexionsToday);
        stats.put("twoFACount", twoFACount);
        stats.put("blockedCount", blockedCount);
        stats.put("avgMlScore", avgMlScore != null ? Math.round(avgMlScore) : 0);
        stats.put("suspendedCount", suspendedCount);

        return ResponseEntity.ok(stats);
    }

    // ============================================================
    // COMPTES SUSPENDUS
    // ============================================================
    @GetMapping("/suspended-users")
    public ResponseEntity<?> getSuspendedUsers() {
        List<User> suspendedUsers = userRepository.findByStatut(Statut.SUSPENDU);

        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : suspendedUsers) {
            Optional<LoginHistory> lastLogin = loginHistoryRepository
                    .findTopByUserIdAndIsSuccessfulFalseOrderByLoginTimeDesc(user.getId());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("nom", user.getNom());
            userInfo.put("prenom", user.getPrenom());

            if (lastLogin.isPresent()) {
                LoginHistory log = lastLogin.get();
                userInfo.put("reason", log.getFailureReason());
                userInfo.put("date", log.getLoginTime());
                userInfo.put("riskScore", log.getRiskScore());
                userInfo.put("isolationScore", log.getIsolationScore());
                userInfo.put("randomForestScore", log.getRandomForestScore());
                userInfo.put("ruleTriggered", log.getRuleTriggered());
                userInfo.put("mlDecision", log.getMlDecision());
            }

            result.add(userInfo);
        }

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // RÉACTIVER UN COMPTE (avec nettoyage des logs d'échec)
    // ============================================================
    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Utilisateur non trouvé"));
        }

        User user = userOpt.get();
        user.setStatut(Statut.ACTIF);
        user.setBlockedAt(null);
        userRepository.save(user);

        // ✅ Supprimer les logs d'échec pour réinitialiser le compteur failed_attempts
        List<LoginHistory> failedLogs = loginHistoryRepository.findByUserIdAndIsSuccessfulFalseOrderByLoginTimeDesc(user.getId());
        for (LoginHistory log : failedLogs) {
            loginHistoryRepository.delete(log);
        }

        System.out.println("✅ Compte " + user.getEmail() + " réactivé et historique d'échecs nettoyé");

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ACCOUNT_REACTIVATED");
        notification.put("userId", user.getId());
        notification.put("email", user.getEmail());
        notification.put("timestamp", LocalDateTime.now());
        sendToAllEmitters("reactivated", notification);

        return ResponseEntity.ok(Map.of("success", true, "message", "Compte réactivé"));
    }

    // ============================================================
    // DERNIÈRES CONNEXIONS
    // ============================================================
    @GetMapping("/recent-logins")
    public ResponseEntity<?> getRecentLogins(@RequestParam(defaultValue = "50") int limit) {
        List<LoginHistory> recentLogins = loginHistoryRepository.findTopNOrderByLoginTimeDesc(limit);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LoginHistory log : recentLogins) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", log.getId());
            entry.put("userEmail", log.getUser().getEmail());
            entry.put("userName", log.getUser().getNom() + " " + log.getUser().getPrenom());
            entry.put("loginTime", log.getLoginTime());
            entry.put("country", log.getCountry());
            entry.put("deviceType", log.getDeviceType());
            entry.put("isolationScore", log.getIsolationScore());
            entry.put("randomForestScore", log.getRandomForestScore());
            entry.put("riskScore", log.getRiskScore());

            String decision = log.getMlDecision();
            if (decision == null) {
                if (log.getIsSuccessful() != null && log.getIsSuccessful()) {
                    decision = "NORMAL";
                } else if (log.getTwoFactorRequired() != null && log.getTwoFactorRequired()) {
                    decision = "2FA_REQUIRED";
                } else {
                    decision = "FAILED";
                }
            }
            entry.put("decision", decision);
            entry.put("ruleTriggered", log.getRuleTriggered());
            entry.put("isSuccessful", log.getIsSuccessful());
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // DISTRIBUTION DES DÉCISIONS
    // ============================================================
    @GetMapping("/decision-distribution")
    public ResponseEntity<?> getDecisionDistribution() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<LoginHistory> logs = loginHistoryRepository.findByLoginTimeAfter(sevenDaysAgo);

        long normal = logs.stream().filter(l -> "NORMAL".equals(l.getMlDecision())).count();
        long twoFA = logs.stream().filter(l -> "2FA_REQUIRED".equals(l.getMlDecision()) ||
                (l.getTwoFactorRequired() != null && l.getTwoFactorRequired())).count();
        long blocked = logs.stream().filter(l -> "BLOCKED".equals(l.getMlDecision())).count();

        long total = normal + twoFA + blocked;

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("normal", total > 0 ? (normal * 100 / total) : 0);
        distribution.put("twoFA", total > 0 ? (twoFA * 100 / total) : 0);
        distribution.put("blocked", total > 0 ? (blocked * 100 / total) : 0);

        return ResponseEntity.ok(distribution);
    }
}