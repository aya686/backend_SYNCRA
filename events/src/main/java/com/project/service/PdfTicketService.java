package com.project.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Service
public class PdfTicketService {
    // Ajoute cette méthode à PdfTicketService.java
    public byte[] generateFormationTicketBytes(String nom, String email, String formationTitle,
                                               String formationNiveau, int dureeHeures, double prix,
                                               Long inscriptionId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Police
            BaseFont baseFont = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 20, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);
            Font headerFont = new Font(baseFont, 14, Font.BOLD);

            // En-tête
            Paragraph title = new Paragraph("CERTIFICAT D'INSCRIPTION - FORMATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph logo = new Paragraph("Event&Formation", headerFont);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(30);
            document.add(logo);

            Paragraph line = new Paragraph("=========================================");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);

            // Informations participant
            document.add(new Paragraph("INFORMATIONS PARTICIPANT", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Nom complet : " + nom, normalFont));
            document.add(new Paragraph("Email : " + email, normalFont));
            document.add(new Paragraph(" "));

            // Informations formation
            document.add(new Paragraph("DÉTAILS DE LA FORMATION", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Titre : " + formationTitle, boldFont));
            document.add(new Paragraph("Niveau : " + formationNiveau, normalFont));
            document.add(new Paragraph("Durée : " + dureeHeures + " heures", normalFont));
            if (prix > 0) {
                document.add(new Paragraph("Prix : " + prix + " €", normalFont));
            } else {
                document.add(new Paragraph("Prix : GRATUIT", boldFont));
            }
            document.add(new Paragraph(" "));

            // Informations inscription
            document.add(new Paragraph("INFORMATIONS D'INSCRIPTION", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Numéro d'inscription : " + inscriptionId, boldFont));
            document.add(new Paragraph("Date d'inscription : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Statut : CONFIRMÉE", boldFont));
            document.add(new Paragraph(" "));

            // Pied de page
            document.add(line);
            Paragraph footer = new Paragraph("Ce certificat atteste de votre inscription à la formation.\nPrésentez-le le jour de la formation.", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Génère le PDF en mémoire et retourne les bytes
    public byte[] generateTicketBytes(String nom, String email, String eventTitle,
                                      String eventDate, String eventLieu,
                                      Long inscriptionId, double prix) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Police
            BaseFont baseFont = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 20, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);
            Font headerFont = new Font(baseFont, 14, Font.BOLD);

            // URL du ticket local
            String ticketUrl = "http://localhost:8089/event_db/api/inscriptions/ticket/" + inscriptionId;

            // QR Code
            BarcodeQRCode qrCode = new BarcodeQRCode(ticketUrl, 120, 120, null);
            Image qrImage = qrCode.getImage();
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(20);
            qrImage.setSpacingAfter(20);

            // En-tête
            Paragraph title = new Paragraph("TICKET DE CONFIRMATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Logo
            Paragraph logo = new Paragraph("Event&Formation", headerFont);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(30);
            document.add(logo);

            // Ligne
            Paragraph line = new Paragraph("=========================================");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);

            // Informations participant
            document.add(new Paragraph("INFORMATIONS PARTICIPANT", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Nom complet : " + nom, normalFont));
            document.add(new Paragraph("Email : " + email, normalFont));
            document.add(new Paragraph(" "));

            // Informations événement
            document.add(new Paragraph("DÉTAILS DE L'ÉVÉNEMENT", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Titre : " + eventTitle, boldFont));
            document.add(new Paragraph("Date : " + eventDate, normalFont));
            document.add(new Paragraph("Lieu : " + eventLieu, normalFont));
            document.add(new Paragraph(" "));

            // Informations inscription
            document.add(new Paragraph("INFORMATIONS D'INSCRIPTION", headerFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Numéro d'inscription : " + inscriptionId, boldFont));
            document.add(new Paragraph("Date d'inscription : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Statut : CONFIRMÉE", boldFont));

            if (prix > 0) {
                document.add(new Paragraph("Montant payé : " + prix + " €", normalFont));
            } else {
                document.add(new Paragraph("Montant : GRATUIT", boldFont));
            }
            document.add(new Paragraph(" "));

            // QR Code
            document.add(new Paragraph("Scannez ce QR code pour accéder à votre ticket :", normalFont));
            document.add(qrImage);

            // Pied de page
            document.add(new Paragraph(" "));
            document.add(line);
            Paragraph footer = new Paragraph("Ce ticket vous est délivré pour confirmer votre inscription.\nPrésentez-le à l'entrée de l'événement.", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}