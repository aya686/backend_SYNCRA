package com.synchub.ms6.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.messaging.service.sid:}")
    private String messagingServiceSid;

    @Value("${twilio.enabled:false}")
    private boolean enabled;

    @Value("${twilio.admin.phone:+21696619052}")
    private String adminPhone;

    @Value("${twilio.from.phone:+17406608964}")
    private String fromPhone;

    @PostConstruct
    public void init() {
        if (enabled && !accountSid.isEmpty() && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.info("Twilio SMS service is disabled or not configured");
        }
    }

    public void sendSms(String toPhoneNumber, String message) {
        if (!enabled || accountSid.isEmpty() || authToken.isEmpty()) {
            log.warn("SMS service is disabled or not configured. Message not sent: {}", message);
            return;
        }

        log.info("Attempting to send SMS to: {} (MessagingServiceSID: {})", toPhoneNumber, messagingServiceSid);
        log.info("Message length: {} characters", message.length());

        try {
            Message smsMessage;
            if (messagingServiceSid != null && !messagingServiceSid.isEmpty()) {
                log.info("Using Messaging Service SID: {}", messagingServiceSid);
                smsMessage = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        messagingServiceSid,
                        message
                ).create();
            } else {
                log.info("Messaging Service not configured, using direct number: {}", fromPhone);
                smsMessage = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber(fromPhone),
                        message
                ).create();
            }
            log.info("SMS created successfully. SID: {}, Status: {}, Price: {}, ErrorCode: {}", 
                    smsMessage.getSid(), 
                    smsMessage.getStatus(),
                    smsMessage.getPrice(),
                    smsMessage.getErrorCode());
            
            // Vérifier si le message a été accepté
            if ("accepted".equals(smsMessage.getStatus()) || "queued".equals(smsMessage.getStatus())) {
                log.info("SMS is being processed by Twilio. It may take a few moments to be delivered.");
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
        }
    }

    public void sendOrderConfirmationWithPromo(String clientPhone, Long orderId, Double originalAmount, 
                                                Double discountAmount, Double finalAmount, String promoCode) {
        StringBuilder message = new StringBuilder();
        message.append("[OK] Commande #").append(orderId).append(" confirmée!\n\n");
        message.append("Détails de votre commande:\n");
        message.append("Montant original: ").append(String.format("%.2f TND", originalAmount)).append("\n");
        
        if (promoCode != null && !promoCode.isEmpty()) {
            message.append("Code promo: ").append(promoCode).append("\n");
            message.append("Remise: -").append(String.format("%.2f TND", discountAmount)).append("\n");
        }
        
        message.append("Total payé: ").append(String.format("%.2f TND", finalAmount)).append("\n\n");
        message.append("Merci pour votre confiance!");

        sendSms(clientPhone, message.toString());
    }

    public void sendOrderConfirmation(String clientPhone, Long orderId, Double amount) {
        String message = String.format(
            "[OK] Commande #%d confirmée!\nMontant: %.2f TND\n\nMerci pour votre confiance!",
            orderId, amount
        );
        sendSms(clientPhone, message);
    }

    public String getAdminPhone() {
        return adminPhone;
    }
}
