package tn.esprit.pifirst.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.service.AuthService;
import tn.esprit.pifirst.service.GeoIpService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private GeoIpService geoIpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        AuthService.AuthResult result = authService.login(email, password, request, response);

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", result.getToken(),
                    "user", result.getUser(),
                    "riskScore", result.getRiskScore(),
                    "decision", result.getDecision(),
                    "rule_triggered", result.getRuleTriggered()
            ));
        } else if (result.isNeeds2FA()) {
            // ✅ Retourner le QR code pour TOTP
            return ResponseEntity.ok(Map.of(
                    "needs2FA", true,
                    "userId", result.getUserId(),
                    "riskScore", result.getRiskScore(),
                    "message", result.getMessage(),
                    "decision", "2FA_REQUIRED",
                    "rule_triggered", "rule_2fa",
                    "qrCode", result.getTwoFACode()  // QR code en base64
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.getMessage()
            ));
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody Map<String, Object> request,
                                       HttpServletResponse response) {
        Long userId = ((Number) request.get("userId")).longValue();
        String code = (String) request.get("code");

        System.out.println("🔵 Controller verify2FA - userId: " + userId + ", code: " + code);

        AuthService.AuthResult result = authService.verify2FA(userId, code, response);

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", result.getToken(),
                    "user", result.getUser()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", result.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        String cookie = String.format("jwt=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict");
        response.addHeader("Set-Cookie", cookie);
        return ResponseEntity.ok(Map.of("success", true, "message", "Déconnexion réussie"));
    }

    @GetMapping("/test-geo/{ip}")
    public ResponseEntity<?> testGeo(@PathVariable String ip) {
        System.out.println("🔍 Test géolocalisation pour IP: " + ip);
        GeoIpService.GeoInfo info = geoIpService.getGeoInfo(ip);
        if (info != null) {
            return ResponseEntity.ok(info);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Impossible de géolocaliser l'IP " + ip,
                    "ip", ip
            ));
        }
    }
}