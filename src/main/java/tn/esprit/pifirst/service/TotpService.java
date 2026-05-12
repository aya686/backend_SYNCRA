package tn.esprit.pifirst.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TotpService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    /**
     * Génère un nouveau secret TOTP
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Génère le QR code en base64 pour Google Authenticator
     */
    public String generateQrCode(String secret, String email) {
        try {
            QrData data = new QrData.Builder()
                    .label(email)
                    .secret(secret)
                    .issuer("PIFirst")
                    .build();

            // ✅ CORRECTION : byte[] → String Base64
            byte[] qrCodeBytes = qrGenerator.generate(data);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + qrCodeBase64;

        } catch (QrGenerationException e) {
            System.err.println("❌ Erreur génération QR code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Vérifie si le code saisi est valide
     */
    public boolean isValidCode(String secret, String code) {
        if (secret == null || code == null) return false;
        return verifier.isValidCode(secret, code);
    }
}