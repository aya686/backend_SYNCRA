package com.synchub.ms6.service;

import com.synchub.ms6.dto.BoutiqueDTOs;
import com.synchub.ms6.entity.Boutique;
import com.synchub.ms6.entity.Configuration;
import com.synchub.ms6.entity.StatsBoutique;
import com.synchub.ms6.mapper.BoutiqueMapper;
import com.synchub.ms6.repository.BoutiqueRepository;
import com.synchub.ms6.repository.ConfigurationRepository;
import com.synchub.ms6.repository.StatsBoutiqueRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final ConfigurationRepository configurationRepository;
    private final StatsBoutiqueRepository statsBoutiqueRepository;

    @Transactional
    public BoutiqueDTOs.BoutiqueResponse createBoutique(BoutiqueDTOs.BoutiqueRequest request) {
        Boutique boutique = BoutiqueMapper.toEntity(request);
        boutique.setStatut(Boutique.StatutBoutique.PENDING);

        Boutique savedBoutique = boutiqueRepository.save(boutique);

        // Auto-generate empty stats
        StatsBoutique stats = StatsBoutique.builder()
                .boutique(savedBoutique)
                .totalVentes(0.0)
                .totalCommandes(0)
                .noteMoyenne(0.0)
                .build();
        statsBoutiqueRepository.save(stats);

        // Create default configuration
        Configuration config = Configuration.builder()
                .boutique(savedBoutique)
                .livraison("Standard")
                .paiementAccepte("Carte, Virement")
                .politique("Politique standard de la boutique")
                .langue("fr")
                .build();
        configurationRepository.save(config);

        return BoutiqueMapper.toResponse(savedBoutique);
    }

    public List<BoutiqueDTOs.BoutiqueResponse> getAllBoutiques() {
        return boutiqueRepository.findAll().stream()
                .map(BoutiqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BoutiqueDTOs.BoutiqueResponse getBoutiqueById(Long id) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + id));
        return BoutiqueMapper.toResponse(boutique);
    }

    @Transactional
    public BoutiqueDTOs.BoutiqueResponse updateBoutique(Long id, BoutiqueDTOs.BoutiqueRequest request) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + id));

        boutique.setNom(request.getNom());
        boutique.setDescription(request.getDescription());
        boutique.setTheme(request.getTheme());
        boutique.setLogo(request.getLogo());

        Boutique updated = boutiqueRepository.save(boutique);
        return BoutiqueMapper.toResponse(updated);
    }

    @Transactional
    public BoutiqueDTOs.BoutiqueResponse toggleSuspendBoutique(Long id) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + id));

        if (boutique.getStatut() == Boutique.StatutBoutique.SUSPENDED) {
            boutique.setStatut(Boutique.StatutBoutique.ACTIVE);
        } else {
            boutique.setStatut(Boutique.StatutBoutique.SUSPENDED);
        }

        Boutique updated = boutiqueRepository.save(boutique);
        return BoutiqueMapper.toResponse(updated);
    }

    @Transactional
    public void deleteBoutique(Long id) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + id));
        boutiqueRepository.delete(boutique);
    }

    public BoutiqueDTOs.StatsBoutiqueResponse getBoutiqueStats(Long boutiqueId) {
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + boutiqueId));

        return statsBoutiqueRepository.findTopByBoutiqueBoutiqueIdOrderByDateCalculDesc(boutiqueId)
                .map(BoutiqueMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Statistiques non trouvées"));
    }

    @Transactional
    public BoutiqueDTOs.ConfigurationResponse updateConfiguration(Long boutiqueId, BoutiqueDTOs.ConfigurationRequest request) {
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée: " + boutiqueId));

        Configuration config = configurationRepository.findByBoutiqueBoutiqueId(boutiqueId)
                .orElseGet(() -> Configuration.builder().boutique(boutique).build());

        config.setLivraison(request.getLivraison());
        config.setPaiementAccepte(request.getPaiementAccepte());
        config.setPolitique(request.getPolitique());
        config.setLangue(request.getLangue());

        Configuration saved = configurationRepository.save(config);
        return BoutiqueMapper.toResponse(saved);
    }

    public BoutiqueDTOs.ConfigurationResponse getConfiguration(Long boutiqueId) {
        Configuration config = configurationRepository.findByBoutiqueBoutiqueId(boutiqueId)
                .orElseThrow(() -> new EntityNotFoundException("Configuration non trouvée"));
        return BoutiqueMapper.toResponse(config);
    }

    // Business rule: Calculate note moyenne from order ratings
    @Transactional
    public Double calculateNoteMoyenne(Long boutiqueId) {
        // In a real scenario, this would fetch ratings from commandes/livraisons
        // For now, we simulate the calculation
        return statsBoutiqueRepository.findTopByBoutiqueBoutiqueIdOrderByDateCalculDesc(boutiqueId)
                .map(StatsBoutique::getNoteMoyenne)
                .orElse(0.0);
    }
}
