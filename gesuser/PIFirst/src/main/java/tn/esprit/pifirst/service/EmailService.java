package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void send2FACode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Code de vérification - MS4 Platform");
        message.setText("Bonjour,\n\n"
                + "Votre code de vérification est : " + code + "\n\n"
                + "Ce code expire dans 5 minutes.\n\n"
                + "Si vous n'êtes pas à l'origine de cette connexion, ignorez cet email.\n\n"
                + "Cordialement,\n"
                + "L'équipe MS4");
        mailSender.send(message);
    }
}