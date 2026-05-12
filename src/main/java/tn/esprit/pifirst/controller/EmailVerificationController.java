package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.service.EmailVerificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/email-verification")
@CrossOrigin(origins = "*")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            emailVerificationService.envoyerCodeVerification(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Code envoyé à " + email,
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code  = body.get("code");
        boolean valide = emailVerificationService.verifierCode(email, code);
        if (valide) {
            return ResponseEntity.ok(Map.of("valide", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "valide", false,
                    "error", "Code incorrect ou expiré"
            ));
        }
    }
}