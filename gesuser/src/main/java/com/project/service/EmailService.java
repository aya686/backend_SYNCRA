package com.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfTicketService pdfTicketService;
    // Ajoute cette méthode à EmailService.java
    public void sendFormationConfirmationEmail(String to, String nom, String formationTitle,
                                               String formationNiveau, int dureeHeures, double prix,
                                               Long inscriptionId) {
        try {
            System.out.println("========================================");
            System.out.println("📧 ENVOI D'EMAIL CONFIRMATION FORMATION");
            System.out.println("========================================");
            System.out.println("Destinataire: " + to);

            // Générer le PDF de la formation
            byte[] pdfBytes = pdfTicketService.generateFormationTicketBytes(
                    nom, to, formationTitle, formationNiveau, dureeHeures, prix, inscriptionId);

            if (pdfBytes == null) {
                throw new RuntimeException("Erreur lors de la génération du PDF");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Confirmation d'inscription - Formation " + formationTitle);
            helper.setFrom("sadokslama20@gmail.com");

            String htmlContent = buildFormationEmailContent(nom, formationTitle, formationNiveau, dureeHeures, prix);
            helper.setText(htmlContent, true);

            helper.addAttachment("Certificat_Formation_" + inscriptionId + ".pdf",
                    new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            System.out.println("✅ Email avec certificat envoyé avec succès à: " + to);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildFormationEmailContent(String nom, String formationTitle, String formationNiveau,
                                              int dureeHeures, double prix) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<h2 style='color: #4f46e5;'>🎓 Confirmation d'inscription - Formation</h2>" +
                "<p>Bonjour <strong>" + nom + "</strong>,</p>" +
                "<p>Votre inscription à la formation <strong>\"" + formationTitle + "\"</strong> a été confirmée avec succès !</p>" +
                "<div style='background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                "<p><strong>📚 Niveau :</strong> " + formationNiveau + "</p>" +
                "<p><strong>⏱️ Durée :</strong> " + dureeHeures + " heures</p>" +
                "<p><strong>💰 Prix :</strong> " + (prix == 0 ? "Gratuit" : prix + " €") + "</p>" +
                "</div>" +
                "<p>Vous trouverez ci-joint votre <strong>certificat d'inscription</strong>.</p>" +
                "<p>Cordialement,<br>L'équipe Event&Formation</p>" +
                "</div></body></html>";
    }
    public void sendConfirmationEmailWithPdf(String to, String nom, String eventTitle,
                                      String eventDate, String eventLieu,
                                      Long inscriptionId, double prix) {
        try {
            System.out.println("========================================");
            System.out.println("📧 ENVOI D'EMAIL AVEC TICKET PDF");
            System.out.println("========================================");
            System.out.println("Destinataire: " + to);

            // Générer le PDF
            byte[] pdfBytes = pdfTicketService.generateTicketBytes(nom, to, eventTitle, eventDate, eventLieu, inscriptionId, prix);

            if (pdfBytes == null) {
                throw new RuntimeException("Erreur lors de la génération du PDF");
            }

            // Créer l'email avec pièce jointe
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Confirmation d'inscription - " + eventTitle);
            helper.setFrom("sadokslama20@gmail.com");

            // Contenu HTML
            String htmlContent = "<!DOCTYPE html>" +
                    "<html><head><meta charset='UTF-8'></head>" +
                    "<body style='font-family: Arial, sans-serif;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                    "<h2 style='color: #4f46e5;'>✅ Confirmation d'inscription</h2>" +
                    "<p>Bonjour <strong>" + nom + "</strong>,</p>" +
                    "<p>Votre inscription à l'événement <strong>\"" + eventTitle + "\"</strong> a été confirmée avec succès !</p>" +
                    "<div style='background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                    "<p><strong>📅 Date :</strong> " + eventDate + "</p>" +
                    "<p><strong>📍 Lieu :</strong> " + eventLieu + "</p>" +
                    "<p><strong>🎫 Numéro :</strong> " + inscriptionId + "</p>" +
                    "</div>" +
                    "<p>Vous trouverez ci-joint votre <strong>ticket de confirmation</strong>.</p>" +
                    "<p>Cordialement,<br>L'équipe Event&Formation</p>" +
                    "</div></body></html>";

            helper.setText(htmlContent, true);

            // Ajouter le PDF en pièce jointe
            helper.addAttachment("Ticket_" + inscriptionId + ".pdf", new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            System.out.println("✅ Email avec ticket PDF envoyé avec succès à: " + to);
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}