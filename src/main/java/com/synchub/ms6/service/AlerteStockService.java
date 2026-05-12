package com.synchub.ms6.service;

import com.synchub.ms6.entity.AlerteStock;
import com.synchub.ms6.entity.Stock;
import com.synchub.ms6.repository.AlerteStockRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteStockService {

    private final JavaMailSender mailSender;
    private final AlerteStockRepository alerteStockRepository;

    @Value("${alerte.stock.admin-email:mhadhhich@gmail.com}")
    private String adminEmail;

    @Value("${alerte.stock.nom-expediteur:SyncHub E-commerce}")
    private String nomExpediteur;

    /**
     * Envoie une alerte par email quand le stock atteint ou dépasse le seuil d'alerte
     */
    @Async
    @Transactional
    public void envoyerAlerteStockBas(Stock stock, int quantiteAvant) {
        if (stock == null || stock.getProduit() == null) {
            log.warn("Impossible d'envoyer l'alerte: stock ou produit null");
            return;
        }

        try {
            String produitNom = stock.getProduit().getNom();
            int quantite = stock.getQuantite();
            int seuil = stock.getSeuilAlerte();
            String boutiqueNom = stock.getProduit().getBoutique() != null 
                ? stock.getProduit().getBoutique().getNom() 
                : "Inconnue";

            String sujet = String.format("ALERTE STOCK - %s (%d unités restantes)", produitNom, quantite);
            
            String contenuHtml = construireEmailHtmlAlerteStock(stock, produitNom, boutiqueNom, quantite, seuil);
            
            // Enregistrer l'alerte en base
            AlerteStock alerte = AlerteStock.builder()
                    .produit(stock.getProduit())
                    .stock(stock)
                    .quantiteAvant(quantiteAvant)
                    .quantiteApres(quantite)
                    .seuilAlerte(seuil)
                    .type(AlerteStock.TypeAlerte.STOCK_BAS)
                    .emailEnvoye(true)
                    .message("Stock sous le seuil d'alerte: " + quantite + " <= " + seuil)
                    .build();
            alerteStockRepository.save(alerte);
            
            envoyerEmailHtml(adminEmail, sujet, contenuHtml);
            
            log.info("Alerte stock envoyée et enregistrée pour le produit: {} (quantité: {}, seuil: {})", 
                    produitNom, quantite, seuil);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte de stock", e);
        }
    }

    /**
     * Envoie une alerte urgente quand le stock est complètement épuisé
     */
    @Async
    @Transactional
    public void envoyerAlerteStockEpuise(Stock stock, int quantiteAvant) {
        if (stock == null || stock.getProduit() == null) {
            log.warn("Impossible d'envoyer l'alerte: stock ou produit null");
            return;
        }

        try {
            String produitNom = stock.getProduit().getNom();
            String boutiqueNom = stock.getProduit().getBoutique() != null 
                ? stock.getProduit().getBoutique().getNom() 
                : "Inconnue";

            String sujet = String.format("URGENT - STOCK EPUISÉ - %s", produitNom);
            
            String contenuHtml = construireEmailHtmlStockEpuise(stock, produitNom, boutiqueNom);
            
            // Enregistrer l'alerte en base
            AlerteStock alerte = AlerteStock.builder()
                    .produit(stock.getProduit())
                    .stock(stock)
                    .quantiteAvant(quantiteAvant)
                    .quantiteApres(0)
                    .seuilAlerte(stock.getSeuilAlerte())
                    .type(AlerteStock.TypeAlerte.STOCK_EPUIS)
                    .emailEnvoye(true)
                    .message("STOCK COMPLETEMENT EPUISÉ")
                    .build();
            alerteStockRepository.save(alerte);
            
            envoyerEmailHtml(adminEmail, sujet, contenuHtml);
            
            log.info("Alerte stock épuisé envoyée et enregistrée pour le produit: {}", produitNom);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte de stock épuisé", e);
        }
    }

    /**
     * Envoie une notification de restock (après annulation de commande)
     */
    @Async
    public void envoyerNotificationRestock(Stock stock, int quantiteRestauree) {
        if (stock == null || stock.getProduit() == null) {
            return;
        }

        try {
            String produitNom = stock.getProduit().getNom();
            int nouvelleQuantite = stock.getQuantite();
            
            String sujet = String.format("Restock - %s (+ %d unités)", produitNom, quantiteRestauree);
            
            String contenu = String.format(
                "Bonjour,\n\n" +
                "Suite à une annulation de commande, le stock du produit '%s' a été restauré.\n\n" +
                "Nouvelle quantité disponible: %d unités\n" +
                "Quantité restaurée: %d unités\n\n" +
                "Cordialement,\n%s",
                produitNom, nouvelleQuantite, quantiteRestauree, nomExpediteur
            );
            
            envoyerEmailSimple(adminEmail, sujet, contenu);
            
            log.info("Notification restock envoyée pour le produit: {} (quantité restaurée: {})", 
                    produitNom, quantiteRestauree);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de restock", e);
        }
    }

    /**
     * Vérifie et envoie l'alerte appropriée selon le niveau de stock
     * @param stock le stock à vérifier
     * @param quantiteAvant quantité avant la modification (pour l'historique)
     */
    @Transactional
    public void verifierEtEnvoyerAlerte(Stock stock, int quantiteAvant) {
        if (stock == null) {
            return;
        }

        int quantite = stock.getQuantite();
        
        if (quantite == 0) {
            envoyerAlerteStockEpuise(stock, quantiteAvant);
        } else if (quantite <= stock.getSeuilAlerte()) {
            envoyerAlerteStockBas(stock, quantiteAvant);
        }
    }

    /**
     * Récupère toutes les alertes d'un produit
     */
    public List<AlerteStock> getAlertesByProduit(Long produitId) {
        return alerteStockRepository.findByProduitProduitId(produitId);
    }

    /**
     * Récupère toutes les alertes récentes (7 derniers jours)
     */
    public List<AlerteStock> getAlertesRecentes() {
        LocalDateTime dateMin = LocalDateTime.now().minusDays(7);
        return alerteStockRepository.findByDateBetween(dateMin, LocalDateTime.now());
    }

    /**
     * Teste la configuration email
     */
    public void testerEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(adminEmail);
            message.setSubject("Test - Configuration Email SyncHub");
            message.setText("Ceci est un email de test pour vérifier la configuration SMTP.\n\nSi vous recevez cet email, la configuration est correcte!\n\nCordialement,\n" + nomExpediteur);
            
            mailSender.send(message);
            log.info("Email de test envoyé avec succès à {}", adminEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de test", e);
            throw new RuntimeException("Erreur de configuration email: " + e.getMessage());
        }
    }

    private void envoyerEmailHtml(String to, String sujet, String contenuHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        try {
            helper.setFrom(adminEmail, nomExpediteur);
        } catch (UnsupportedEncodingException e) {
            // Fallback sans nom d'expéditeur personnalisé
            helper.setFrom(adminEmail);
        }
        helper.setTo(to);
        helper.setSubject(sujet);
        helper.setText(contenuHtml, true);
        
        mailSender.send(message);
    }

    private void envoyerEmailSimple(String to, String sujet, String contenu) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(contenu);
        
        mailSender.send(message);
    }

    private String construireEmailHtmlAlerteStock(Stock stock, String produitNom, String boutiqueNom, 
                                                   int quantite, int seuil) {
        String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String entrepot = stock.getEntrepot() != null ? stock.getEntrepot() : "Non spécifié";
        
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
            ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #ff6b6b, #ee5a5a); color: white; padding: 30px; text-align: center; }" +
            ".header h1 { margin: 0; font-size: 24px; }" +
            ".content { padding: 30px; }" +
            ".alert-box { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
            ".info-box { background-color: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 4px; }" +
            ".info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #dee2e6; }" +
            ".info-row:last-child { border-bottom: none; }" +
            ".label { font-weight: bold; color: #495057; }" +
            ".value { color: #212529; }" +
            ".quantity-warning { color: #dc3545; font-weight: bold; font-size: 18px; }" +
            ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }" +
            ".button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>⚠️ Alerte de Stock</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<div class='alert-box'>" +
            "<strong>Attention!</strong> Le stock du produit <strong>%s</strong> est sous le seuil d'alerte." +
            "</div>" +
            "<div class='info-box'>" +
            "<div class='info-row'><span class='label'>Produit:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Boutique:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Entrepôt:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Quantité actuelle:</span><span class='quantity-warning'>%d unités</span></div>" +
            "<div class='info-row'><span class='label'>Seuil d'alerte:</span><span class='value'>%d unités</span></div>" +
            "<div class='info-row'><span class='label'>Date/Heure:</span><span class='value'>%s</span></div>" +
            "</div>" +
            "<p style='text-align: center;'>" +
            "<a href='#' class='button'>Gérer les stocks</a>" +
            "</p>" +
            "</div>" +
            "<div class='footer'>" +
            "Cet email a été envoyé automatiquement par %s<br>" +
            "Date: %s" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            produitNom, produitNom, boutiqueNom, entrepot, quantite, seuil, dateHeure, nomExpediteur, dateHeure
        );
    }

    private String construireEmailHtmlStockEpuise(Stock stock, String produitNom, String boutiqueNom) {
        String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String entrepot = stock.getEntrepot() != null ? stock.getEntrepot() : "Non spécifié";
        
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
            ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 30px; text-align: center; }" +
            ".header h1 { margin: 0; font-size: 24px; }" +
            ".content { padding: 30px; }" +
            ".alert-box { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; border-radius: 4px; color: #721c24; }" +
            ".info-box { background-color: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 4px; }" +
            ".info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #dee2e6; }" +
            ".info-row:last-child { border-bottom: none; }" +
            ".label { font-weight: bold; color: #495057; }" +
            ".value { color: #212529; }" +
            ".quantity-danger { color: #dc3545; font-weight: bold; font-size: 24px; }" +
            ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }" +
            ".urgent-badge { display: inline-block; background-color: #dc3545; color: white; padding: 5px 15px; border-radius: 20px; font-size: 12px; margin-bottom: 15px; }" +
            ".button { display: inline-block; padding: 12px 24px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🚨 STOCK EPUISE</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<div class='alert-box'>" +
            "<span class='urgent-badge'>URGENT</span><br>" +
            "<strong>Le stock du produit <strong>%s</strong> est complètement épuisé!</strong><br>" +
            "Ce produit n'est plus disponible à la vente." +
            "</div>" +
            "<div class='info-box'>" +
            "<div class='info-row'><span class='label'>Produit:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Boutique:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Entrepôt:</span><span class='value'>%s</span></div>" +
            "<div class='info-row'><span class='label'>Quantité restante:</span><span class='quantity-danger'>0 unités</span></div>" +
            "<div class='info-row'><span class='label'>Date/Heure:</span><span class='value'>%s</span></div>" +
            "</div>" +
            "<p style='text-align: center; color: #721c24; font-weight: bold;'>" +
            "Action requise: Réapprovisionner le stock immédiatement!" +
            "</p>" +
            "<p style='text-align: center;'>" +
            "<a href='#' class='button'>Réapprovisionner maintenant</a>" +
            "</p>" +
            "</div>" +
            "<div class='footer'>" +
            "Cet email a été envoyé automatiquement par %s<br>" +
            "Date: %s" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            produitNom, produitNom, boutiqueNom, entrepot, dateHeure, nomExpediteur, dateHeure
        );
    }
}
