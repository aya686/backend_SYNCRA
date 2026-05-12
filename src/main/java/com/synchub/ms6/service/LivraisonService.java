package com.synchub.ms6.service;

import com.synchub.ms6.dto.LivraisonDTOs;
import com.synchub.ms6.entity.*;
import com.synchub.ms6.mapper.LivraisonMapper;
import com.synchub.ms6.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final CommandeRepository commandeRepository;
    private final RetourRepository retourRepository;
    private final RemboursementRepository remboursementRepository;
    private final WeatherService weatherService;

    @Transactional
    public LivraisonDTOs.LivraisonResponse createLivraison(LivraisonDTOs.LivraisonRequest request) {
        Commande commande = commandeRepository.findById(request.getCommandeId())
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée: " + request.getCommandeId()));

        Livraison livraison = LivraisonMapper.toEntity(request, commande);
        
        // Récupérer la météo pour l'adresse de livraison
        enrichWithWeatherData(livraison);
        
        Livraison saved = livraisonRepository.save(livraison);

        return LivraisonMapper.toResponse(saved);
    }

    public List<LivraisonDTOs.LivraisonResponse> getAllLivraisons() {
        return livraisonRepository.findAll().stream()
                .map(LivraisonMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LivraisonDTOs.LivraisonResponse getLivraisonById(Long id) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée: " + id));
        return LivraisonMapper.toResponse(livraison);
    }

    @Transactional
    public LivraisonDTOs.LivraisonResponse updateLivraison(Long id, LivraisonDTOs.LivraisonUpdateRequest request) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée: " + id));

        if (request.getAdresse() != null) {
            livraison.setAdresse(request.getAdresse());
        }
        if (request.getTransporteur() != null) {
            livraison.setTransporteur(request.getTransporteur());
        }
        if (request.getTracking() != null) {
            livraison.setTracking(request.getTracking());
        }

        Livraison updated = livraisonRepository.save(livraison);
        return LivraisonMapper.toResponse(updated);
    }

    @Transactional
    public void deleteLivraison(Long id) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée: " + id));
        
        // Vérifier s'il y a des retours associés
        List<Retour> retours = retourRepository.findByLivraisonLivraisonId(id);
        if (!retours.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une livraison avec des retours associés");
        }
        
        livraisonRepository.delete(livraison);
        log.info("Livraison supprimée: {}", id);
    }

    @Transactional
    public LivraisonDTOs.LivraisonResponse updateStatut(Long id, String statut) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée: " + id));

        Livraison.StatutLivraison newStatut = Livraison.StatutLivraison.valueOf(statut);
        livraison.setStatut(newStatut);

        // If delivered, update commande status
        if (newStatut == Livraison.StatutLivraison.LIVRE) {
            livraison.setDateLiv(LocalDateTime.now());
            Commande commande = livraison.getCommande();
            commande.setStatut(Commande.StatutCommande.LIVREE);
            commandeRepository.save(commande);
        } else if (newStatut == Livraison.StatutLivraison.EXPEDIE) {
            livraison.setDateExp(LocalDateTime.now());
        }

        Livraison updated = livraisonRepository.save(livraison);
        return LivraisonMapper.toResponse(updated);
    }

    @Transactional
    public LivraisonDTOs.RetourResponse initiateRetour(Long livraisonId, LivraisonDTOs.RetourRequest request) {
        Livraison livraison = livraisonRepository.findById(livraisonId)
                .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée: " + livraisonId));

        if (!livraison.isDelivered()) {
            throw new IllegalStateException("Impossible de retourner une livraison non livrée");
        }

        Retour retour = LivraisonMapper.toEntity(request, livraison);
        Retour saved = retourRepository.save(retour);

        return LivraisonMapper.toResponse(saved);
    }

    public List<LivraisonDTOs.RetourResponse> getRetoursByLivraison(Long livraisonId) {
        return retourRepository.findByLivraisonLivraisonId(livraisonId).stream()
                .map(LivraisonMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LivraisonDTOs.RetourResponse getRetourById(Long id) {
        Retour retour = retourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Retour non trouvé: " + id));
        return LivraisonMapper.toResponse(retour);
    }

    @Transactional
    public LivraisonDTOs.RetourResponse updateRetourStatut(Long id, String statut) {
        Retour retour = retourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Retour non trouvé: " + id));

        Retour.StatutRetour newStatut = Retour.StatutRetour.valueOf(statut);
        retour.setStatut(newStatut);

        // If approved, auto-create remboursement
        if (newStatut == Retour.StatutRetour.APPROUVE) {
            createAutoRemboursement(retour);
        }

        Retour updated = retourRepository.save(retour);
        return LivraisonMapper.toResponse(updated);
    }

    private void createAutoRemboursement(Retour retour) {
        // Calculate refund amount based on commande
        double montant = retour.getLivraison().getCommande().getMontantTotal();

        Remboursement remboursement = Remboursement.builder()
                .retour(retour)
                .montant(montant)
                .methode(Remboursement.MethodeRemboursement.CARTE)
                .statut(Remboursement.StatutRemboursement.EN_ATTENTE)
                .build();

        remboursementRepository.save(remboursement);
    }

    @Transactional
    public LivraisonDTOs.RemboursementResponse createRemboursement(Long retourId, LivraisonDTOs.RemboursementRequest request) {
        Retour retour = retourRepository.findById(retourId)
                .orElseThrow(() -> new EntityNotFoundException("Retour non trouvé: " + retourId));

        Remboursement remboursement = LivraisonMapper.toEntity(request, retour);
        Remboursement saved = remboursementRepository.save(remboursement);

        return LivraisonMapper.toResponse(saved);
    }

    public LivraisonDTOs.RemboursementResponse getRemboursementByRetourId(Long retourId) {
        Remboursement remboursement = remboursementRepository.findByRetourRetourId(retourId)
                .orElseThrow(() -> new EntityNotFoundException("Remboursement non trouvé pour le retour: " + retourId));
        return LivraisonMapper.toResponse(remboursement);
    }

    /**
     * Enrichit une livraison avec les données météo
     */
    private void enrichWithWeatherData(Livraison livraison) {
        try {
            // Extraire la ville depuis l'adresse (simplifié - prend la dernière partie)
            String address = livraison.getAdresse();
            String city = extractCityFromAddress(address);
            
            if (city != null && !city.isEmpty()) {
                WeatherService.WeatherData weather = weatherService.getCurrentWeather(city);
                
                if (weather != null) {
                    livraison.setWeatherTemp(weather.getTemperature());
                    livraison.setWeatherCondition(weather.getCondition());
                    livraison.setWeatherDescription(weather.getDescription());
                    livraison.setWeatherIcon(weather.getIcon());
                    livraison.setWeatherAlert(!weatherService.isDeliveryConditionsGood(city));
                }
            }
        } catch (Exception e) {
            // Ne pas bloquer la création de livraison si météo indisponible
            log.warn("Impossible de récupérer la météo pour la livraison: {}", e.getMessage());
        }
    }

    /**
     * Extrait la ville depuis une adresse complète
     * Ex: "123 Rue Paris, 75001 Paris, France" → "Paris"
     */
    private String extractCityFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        // Essayer d'extraire la ville (dernière partie avant le pays)
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            // Prendre l'avant-dernière partie (ville)
            String cityPart = parts[parts.length - 2].trim();
            // Enlever le code postal si présent
            return cityPart.replaceAll("\\d+", "").trim();
        }
        
        // Fallback: essayer avec Tunis comme défaut pour les adresses tunisiennes
        if (address.toLowerCase().contains("tunis")) {
            return "Tunis";
        }
        
        return parts[parts.length - 1].trim();
    }
}
