package tn.esprit.pifirst.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import tn.esprit.pifirst.config.JwtUtils;
import tn.esprit.pifirst.entity.*;
import tn.esprit.pifirst.enums.Statut;
import tn.esprit.pifirst.event.BlockedAccountEvent;
import tn.esprit.pifirst.repository.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;

@Service
public class AuthService {

    @Autowired private UserRepository         userRepository;
    @Autowired private LoginHistoryRepository  loginHistoryRepository;
    @Autowired private PasswordEncoder         passwordEncoder;
    @Autowired private JwtUtils                jwtUtils;
    @Autowired private RestTemplate            restTemplate;
    @Autowired private UserAgentParserService  userAgentParserService;
    @Autowired private GeoIpService            geoIpService;
    @Autowired private DistanceService         distanceService;
    @Autowired private TotpService             totpService;
    @Autowired private ApplicationEventPublisher eventPublisher;

    // ============================================================
    // LOGIN PRINCIPAL
    // ============================================================
    public AuthResult login(String email, String password,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        String ip        = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        System.out.println("========================================");
        System.out.println("🔐 TENTATIVE DE CONNEXION");
        System.out.println("  Email: " + email);
        System.out.println("========================================");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("❌ Utilisateur NON trouvé: " + email);
            return AuthResult.failure("Email ou mot de passe incorrect");
        }

        User user = userOpt.get();
        System.out.println("✅ Utilisateur trouvé: " + user.getEmail());

        // ✅ Vérifier si le compte est déjà bloqué
        if (Statut.SUSPENDU.name().equals(String.valueOf(user.getStatut())) ||
                Statut.BANNI.name().equals(String.valueOf(user.getStatut()))) {
            return AuthResult.failure("Votre compte est bloqué. Contactez l'administrateur.");
        }

        // ✅ Vérification du mot de passe
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("❌ MOT DE PASSE INCORRECT pour: " + email);
            saveLog(user, ip, userAgent, false, "Mot de passe incorrect", 0, null, null, null, null, null);

            // ✅ Compter les tentatives échouées dans les dernières 24h
            long failedAttempts = loginHistoryRepository.countByUserIdAndIsSuccessfulFalseAndLoginTimeAfter(
                    user.getId(), LocalDateTime.now().minusHours(24));

            System.out.println("📊 Tentatives échouées (24h): " + failedAttempts);

            // ✅ Bloquer après 5 tentatives
            if (failedAttempts >= 5) {
                user.setStatut(Statut.SUSPENDU);
                user.setBlockedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("🔒 Compte " + email + " bloqué après " + failedAttempts + " tentatives échouées");

                // Notification au dashboard (apiResponse = null car pas d'appel ML)
                eventPublisher.publishEvent(new BlockedAccountEvent(this, user, null));

                return AuthResult.failure("Compte bloqué après trop de tentatives. Réessayez dans 5 minutes ou contactez l'administrateur.");
            }

            return AuthResult.failure("Email ou mot de passe incorrect");
        }

        System.out.println("✅ MOT DE PASSE CORRECT");

        ApiResponse apiResponse = callApiPython(user, ip, userAgent);
        int riskScore = apiResponse.getRiskScore();

        System.out.println("📊 Score de risque pour " + email + " : " + riskScore + "%");
        System.out.println("📊 Décision de l'API: " + apiResponse.getDecision());
        System.out.println("📊 Règle déclenchée: " + apiResponse.getRuleTriggered());

        if (apiResponse.getExplanationText() != null) {
            System.out.println("📖 Explication: " + apiResponse.getExplanationText());
        }
        if (apiResponse.getTopFactors() != null && !apiResponse.getTopFactors().isEmpty()) {
            System.out.println("🎯 Causes principales:");
            for (Map<String, Object> factor : apiResponse.getTopFactors()) {
                System.out.println("   - " + factor.get("feature") + " = " + factor.get("value"));
            }
        }

        if (apiResponse.isBlocked()) {
            saveLog(user, ip, userAgent, false, "Bloqué par ML (score=" + riskScore + ")",
                    riskScore, apiResponse.getIsolationScore(), apiResponse.getRandomForestScore(),
                    apiResponse.getDecision(), apiResponse.getRuleTriggered(), apiResponse.getExplanationText());
            user.setStatut(Statut.SUSPENDU);
            user.setBlockedAt(LocalDateTime.now());
            userRepository.save(user);

            eventPublisher.publishEvent(new BlockedAccountEvent(this, user, apiResponse));

            return AuthResult.failure("Connexion bloquée ! Compte suspendu pour 5 minutes.");
        }

        if (apiResponse.isRequires2fa()) {
            if (user.getTotpSecret() == null || user.getTotpSecret().isEmpty()) {
                String totpSecret = totpService.generateSecret();
                user.setTotpSecret(totpSecret);
                userRepository.save(user);
                System.out.println("🔐 Secret TOTP généré pour " + email);
            }

            String qrCode = totpService.generateQrCode(user.getTotpSecret(), user.getEmail());

            saveLogWith2FA(user, ip, userAgent, riskScore, "TOTP_" + System.currentTimeMillis(),
                    apiResponse.getIsolationScore(), apiResponse.getRandomForestScore(),
                    apiResponse.getDecision(), apiResponse.getRuleTriggered(), apiResponse.getExplanationText());

            return AuthResult.needs2FA(user.getId(), riskScore, qrCode, apiResponse.getDecision(), apiResponse.getRuleTriggered());
        }

        // Connexion normale
        String token = jwtUtils.generateJwtToken(user);
        saveLog(user, ip, userAgent, true, null, riskScore,
                apiResponse.getIsolationScore(), apiResponse.getRandomForestScore(),
                apiResponse.getDecision(), apiResponse.getRuleTriggered(), apiResponse.getExplanationText());
        user.setDerniereConnexion(LocalDateTime.now());
        userRepository.save(user);

        addTokenCookie(response, token);

        return AuthResult.success(token, user, riskScore, apiResponse.getDecision(), apiResponse.getRuleTriggered());
    }

    // ============================================================
    // VÉRIFICATION 2FA - VERSION TOTP
    // ============================================================
    public AuthResult verify2FA(Long userId, String code, HttpServletResponse response) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("❌ verify2FA: Utilisateur non trouvé: " + userId);
            return AuthResult.failure("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        System.out.println("🔵 verify2FA - Utilisateur: " + user.getEmail() + ", code reçu: " + code);

        boolean isValid = totpService.isValidCode(user.getTotpSecret(), code);

        if (!isValid) {
            System.out.println("❌ Code TOTP invalide pour " + user.getEmail());
            return AuthResult.failure("Code 2FA invalide");
        }

        System.out.println("✅ Code TOTP valide pour " + user.getEmail());

        String token = jwtUtils.generateJwtToken(user);
        addTokenCookie(response, token);

        saveLog(user, getClientIp(null), "TOTP_Verification", true, null, 0,
                0, 0, "NORMAL", "totp_validated", null);

        return AuthResult.success(token, user, 0, "NORMAL", "totp_validated");
    }

    // ============================================================
    // APPEL À L'API PYTHON
    // ============================================================
    private ApiResponse callApiPython(User user, String ip, String userAgent) {
        try {
            Map<String, Object> features = extractFeaturesForML(user, ip, userAgent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("features", features);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://localhost:5000/predict", HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                int riskScore = ((Number) responseBody.getOrDefault("risk_score", 0)).intValue();
                String decision = (String) responseBody.getOrDefault("decision", "NORMAL");
                boolean requires2fa = Boolean.TRUE.equals(responseBody.get("requires_2fa"));
                boolean blocked = Boolean.TRUE.equals(responseBody.get("blocked"));

                int isoScore = ((Number) responseBody.getOrDefault("isolation_score", 0)).intValue();
                int rfScore = ((Number) responseBody.getOrDefault("random_forest_score", 0)).intValue();
                String rule = (String) responseBody.getOrDefault("rule_triggered", "none");

                Map<String, Object> explanation = (Map<String, Object>) responseBody.get("explanation");
                String explanationText = null;
                List<Map<String, Object>> topFactors = null;
                Map<String, Object> scoreBreakdown = null;

                if (explanation != null) {
                    explanationText = (String) explanation.get("reason_text");
                    topFactors = (List<Map<String, Object>>) explanation.get("top_factors");
                    scoreBreakdown = (Map<String, Object>) explanation.get("score_breakdown");
                }

                System.out.println("🎯 [API] decision=" + decision + ", risk_score=" + riskScore);
                System.out.println("   rule_triggered=" + rule);
                System.out.println("   ISO=" + isoScore + "%, RF=" + rfScore + "%");
                if (explanationText != null) {
                    System.out.println("   📖 Explication: " + explanationText);
                }

                return new ApiResponse(riskScore, decision, requires2fa, blocked, isoScore, rfScore, rule,
                        explanationText, topFactors, scoreBreakdown);
            }

            return new ApiResponse(0, "NORMAL", false, false, 0, 0, "error", "Réponse API invalide", null, null);

        } catch (Exception e) {
            System.err.println("❌ Erreur appel API Python: " + e.getMessage());
            int fallbackScore = calculateSimpleRiskScore(user, ip, userAgent);
            return new ApiResponse(
                    fallbackScore,
                    fallbackScore >= 70 ? "BLOCKED" : fallbackScore >= 30 ? "2FA_REQUIRED" : "NORMAL",
                    fallbackScore >= 30 && fallbackScore < 70,
                    fallbackScore >= 70,
                    0, 0, "fallback",
                    "Fallback: API Python indisponible", null, null
            );
        }
    }

    // ============================================================
    // EXTRACTION DES FEATURES
    // ============================================================
    private Map<String, Object> extractFeaturesForML(User user, String ip, String userAgent) {
        Map<String, Object> features = new LinkedHashMap<>();

        System.out.println("\n🔍 ========== EXTRACTION DES FEATURES ==========");

        int hour = LocalDateTime.now().getHour();
        features.put("hour", hour);
        features.put("is_night", (hour >= 1 && hour <= 5) ? 1 : 0);

        DayOfWeek day = LocalDateTime.now().getDayOfWeek();
        features.put("is_weekend", (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) ? 1 : 0);

        System.out.println("⏰ hour=" + hour + " is_night=" + features.get("is_night") + " is_weekend=" + features.get("is_weekend"));

        var parsedUA = userAgentParserService.parse(userAgent);
        features.put("device_type_encoded", getDeviceTypeCode(parsedUA.getDeviceType()));
        features.put("os_encoded", getOsCode(parsedUA.getOs()));
        features.put("browser_encoded", getBrowserCode(parsedUA.getBrowser()));

        System.out.println("📱 device=" + parsedUA.getDeviceType() + " os=" + parsedUA.getOs() + " browser=" + parsedUA.getBrowser());

        GeoIpService.GeoInfo geoInfo = geoIpService.getGeoInfo(ip);
        features.put("country_encoded", geoInfo != null ? 1 : 0);
        System.out.println("🌍 country=" + (geoInfo != null ? geoInfo.getCountry() : "inconnu"));

        Optional<LoginHistory> lastSuccessOpt = loginHistoryRepository.findLastSuccessfulLogin(user.getId());

        if (lastSuccessOpt.isPresent()) {
            features.put("is_first_connection", 0);

            LoginHistory lastSuccess = lastSuccessOpt.get();
            long hoursSinceLastLogin = Duration.between(lastSuccess.getLoginTime(), LocalDateTime.now()).toHours();

            System.out.println("📜 DERNIÈRE CONNEXION RÉUSSIE: " + lastSuccess.getLoginTime());
            System.out.println("   Heures écoulées: " + hoursSinceLastLogin);

            boolean sameCountry = lastSuccess.getCountry() == null
                    || (geoInfo != null && geoInfo.getCountry() != null
                    && geoInfo.getCountry().equals(lastSuccess.getCountry()));
            features.put("same_country", sameCountry ? 1 : 0);

            boolean sameDevice = lastSuccess.getDeviceType() == null
                    || parsedUA.getDeviceType().equals(lastSuccess.getDeviceType());
            features.put("same_device", sameDevice ? 1 : 0);

            long failedAttempts = loginHistoryRepository.countByUserIdAndIsSuccessfulFalseAndLoginTimeAfter(
                    user.getId(), lastSuccess.getLoginTime());
            features.put("failed_attempts", (int) Math.min(failedAttempts, 10));

            features.put("hours_since_last_login", (int) Math.min(hoursSinceLastLogin, 999));

            if (geoInfo != null && lastSuccess.getLatitude() != null && lastSuccess.getLongitude() != null) {
                double distance = distanceService.calculateDistance(
                        lastSuccess.getLatitude(), lastSuccess.getLongitude(),
                        geoInfo.getLat(), geoInfo.getLon()
                );
                features.put("distance_km", (int) Math.round(distance));
            } else {
                features.put("distance_km", 0);
            }

            System.out.println("📜 same_country=" + features.get("same_country")
                    + " same_device=" + features.get("same_device")
                    + " failed_attempts=" + features.get("failed_attempts")
                    + " hours_since_last_login=" + features.get("hours_since_last_login")
                    + " distance_km=" + features.get("distance_km"));

        } else {
            features.put("is_first_connection", 1);
            features.put("same_country", 1);
            features.put("same_device", 1);
            features.put("failed_attempts", 0);
            features.put("hours_since_last_login", 0);
            features.put("distance_km", 0);
            System.out.println("⚠️ PREMIÈRE CONNEXION → valeurs neutres");
        }

        System.out.println("📊 FEATURES FINALES : " + features);
        System.out.println("🔍 =================================================\n");

        return features;
    }

    // ============================================================
    // FALLBACK
    // ============================================================
    private int calculateSimpleRiskScore(User user, String ip, String userAgent) {
        int score = 0;
        Optional<LoginHistory> lastLogin = loginHistoryRepository.findLastSuccessfulLogin(user.getId());
        if (lastLogin.isPresent()) {
            LoginHistory last = lastLogin.get();
            if (last.getIpAddress() != null && !last.getIpAddress().equals(ip)) score += 25;
            if (last.getUserAgent() != null && !last.getUserAgent().equals(userAgent)) score += 10;
        }
        int hour = LocalDateTime.now().getHour();
        if (hour >= 23 || hour <= 5) score += 15;
        DayOfWeek day = LocalDateTime.now().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) score += 10;
        long failedAttempts = loginHistoryRepository.countByUserIdAndIsSuccessfulFalseAndLoginTimeAfter(
                user.getId(), LocalDateTime.now().minusHours(24));
        if (failedAttempts > 3) score += 20;
        return Math.min(score, 100);
    }

    // ============================================================
    // ENCODAGES
    // ============================================================
    private int getDeviceTypeCode(String d) {
        if (d == null) return 0;
        switch (d.toLowerCase()) {
            case "mobile": return 1;
            case "tablet": return 2;
            case "bot": return 3;
            default: return 0;
        }
    }

    private int getOsCode(String os) {
        if (os == null) return 0;
        if (os.contains("Windows")) return 0;
        if (os.contains("Mac")) return 1;
        if (os.contains("Linux")) return 2;
        if (os.contains("Android")) return 3;
        if (os.contains("iOS")) return 4;
        return 0;
    }

    private int getBrowserCode(String b) {
        if (b == null) return 0;
        if (b.contains("Chrome")) return 0;
        if (b.contains("Firefox")) return 1;
        if (b.contains("Safari")) return 2;
        if (b.contains("Edge")) return 3;
        return 0;
    }

    // ============================================================
    // LOGS
    // ============================================================
    private void saveLog(User user, String ip, String userAgent, boolean success,
                         String reason, int riskScore,
                         Integer isoScore, Integer rfScore, String mlDecision,
                         String ruleTriggered, String shapReason) {
        LoginHistory log = buildLog(user, ip, userAgent, riskScore);
        log.setIsSuccessful(success);
        log.setFailureReason(reason);
        log.setIsolationScore(isoScore);
        log.setRandomForestScore(rfScore);
        log.setMlDecision(mlDecision);
        log.setRuleTriggered(ruleTriggered);
        log.setShapReason(shapReason);
        loginHistoryRepository.save(log);
    }

    private void saveLogWith2FA(User user, String ip, String userAgent, int riskScore, String code,
                                Integer isoScore, Integer rfScore, String mlDecision,
                                String ruleTriggered, String shapReason) {
        LoginHistory log = buildLog(user, ip, userAgent, riskScore);
        log.setIsSuccessful(false);
        log.setFailureReason("2FA requis");
        log.setTwoFactorRequired(true);
        log.setTwoFactorCode(code);
        log.setTwoFactorValidated(false);
        log.setIsolationScore(isoScore);
        log.setRandomForestScore(rfScore);
        log.setMlDecision(mlDecision);
        log.setRuleTriggered(ruleTriggered);
        log.setShapReason(shapReason);
        loginHistoryRepository.save(log);
    }

    private LoginHistory buildLog(User user, String ip, String userAgent, int riskScore) {
        LoginHistory log = new LoginHistory();
        log.setUser(user);
        log.setIpAddress(ip);
        log.setUserAgent(userAgent);
        log.setLoginTime(LocalDateTime.now());
        log.setRiskScore(riskScore);

        GeoIpService.GeoInfo geo = geoIpService.getGeoInfo(ip);
        if (geo != null) {
            log.setCountry(geo.getCountry());
            log.setCountryCode(geo.getCountryCode());
            log.setCity(geo.getCity());
            log.setRegion(geo.getRegion());
            log.setZip(geo.getZip());
            log.setLatitude(geo.getLat());
            log.setLongitude(geo.getLon());
            log.setIsp(geo.getIsp());
        }

        if (userAgent != null && !userAgent.isEmpty()) {
            var parsed = userAgentParserService.parse(userAgent);
            log.setDeviceType(parsed.getDeviceType());
            log.setOs(parsed.getOs());
            log.setBrowser(parsed.getBrowser());
        }
        return log;
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        String cookie = String.format("jwt=%s; HttpOnly; Path=/; Max-Age=900; SameSite=Strict", token);
        response.addHeader("Set-Cookie", cookie);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private String generate2FACode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void send2FACode(String email, String code) {
        System.out.println("========================================");
        System.out.println("📧 2FA CODE (fallback) pour " + email);
        System.out.println("🔑 Votre code est : " + code);
        System.out.println("========================================");
    }

    // ============================================================
    // CLASSES INTERNES
    // ============================================================
    public static class ApiResponse {
        private final int riskScore;
        private final String decision;
        private final boolean requires2fa;
        private final boolean blocked;
        private final int isolationScore;
        private final int randomForestScore;
        private final String ruleTriggered;
        private final String explanationText;
        private final List<Map<String, Object>> topFactors;
        private final Map<String, Object> scoreBreakdown;

        ApiResponse(int r, String d, boolean fa, boolean bl, int iso, int rf, String rule,
                    String expText, List<Map<String, Object>> topFactors, Map<String, Object> scoreBreakdown) {
            this.riskScore = r;
            this.decision = d;
            this.requires2fa = fa;
            this.blocked = bl;
            this.isolationScore = iso;
            this.randomForestScore = rf;
            this.ruleTriggered = rule;
            this.explanationText = expText;
            this.topFactors = topFactors;
            this.scoreBreakdown = scoreBreakdown;
        }

        public int getRiskScore() { return riskScore; }
        public String getDecision() { return decision; }
        public boolean isRequires2fa() { return requires2fa; }
        public boolean isBlocked() { return blocked; }
        public int getIsolationScore() { return isolationScore; }
        public int getRandomForestScore() { return randomForestScore; }
        public String getRuleTriggered() { return ruleTriggered; }
        public String getExplanationText() { return explanationText; }
        public List<Map<String, Object>> getTopFactors() { return topFactors; }
        public Map<String, Object> getScoreBreakdown() { return scoreBreakdown; }
    }

    public static class AuthResult {
        private boolean success;
        private boolean needs2FA;
        private String token;
        private User user;
        private String message;
        private int riskScore;
        private Long userId;
        private String twoFACode;
        private String decision;
        private String ruleTriggered;

        public static AuthResult success(String token, User user, int riskScore, String decision, String ruleTriggered) {
            AuthResult r = new AuthResult();
            r.success = true;
            r.token = token;
            r.user = user;
            r.riskScore = riskScore;
            r.decision = decision;
            r.ruleTriggered = ruleTriggered;
            return r;
        }

        public static AuthResult needs2FA(Long userId, int riskScore, String qrCode, String decision, String ruleTriggered) {
            AuthResult r = new AuthResult();
            r.needs2FA = true;
            r.userId = userId;
            r.riskScore = riskScore;
            r.twoFACode = qrCode;
            r.message = "Scannez le QR code avec Google Authenticator";
            r.decision = decision;
            r.ruleTriggered = ruleTriggered;
            return r;
        }

        public static AuthResult failure(String message) {
            AuthResult r = new AuthResult();
            r.success = false;
            r.message = message;
            return r;
        }

        public boolean isSuccess() { return success; }
        public boolean isNeeds2FA() { return needs2FA; }
        public String getToken() { return token; }
        public User getUser() { return user; }
        public String getMessage() { return message; }
        public int getRiskScore() { return riskScore; }
        public Long getUserId() { return userId; }
        public String getTwoFACode() { return twoFACode; }
        public String getDecision() { return decision; }
        public String getRuleTriggered() { return ruleTriggered; }

        public void setSuccess(boolean v) { success = v; }
        public void setNeeds2FA(boolean v) { needs2FA = v; }
        public void setToken(String v) { token = v; }
        public void setUser(User v) { user = v; }
        public void setMessage(String v) { message = v; }
        public void setRiskScore(int v) { riskScore = v; }
        public void setUserId(Long v) { userId = v; }
        public void setTwoFACode(String v) { twoFACode = v; }
        public void setDecision(String v) { decision = v; }
        public void setRuleTriggered(String v) { ruleTriggered = v; }
    }
}