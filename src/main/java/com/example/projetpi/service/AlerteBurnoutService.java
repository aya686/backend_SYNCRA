package com.example.projetpi.service;

import com.example.projetpi.entity.AlerteBurnout;
import com.example.projetpi.repository.AlerteBurnoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AlerteBurnoutService {
    @Autowired
    private AlerteBurnoutRepository alerteBurnoutRepository;

    public AlerteBurnout create(AlerteBurnout alerteBurnout) {
        return alerteBurnoutRepository.save(alerteBurnout);
    }

    public List<AlerteBurnout> findAll() {
        return alerteBurnoutRepository.findAll();
    }

    public Optional<AlerteBurnout> findById(Long id) {
        return alerteBurnoutRepository.findById(id);
    }

    public AlerteBurnout update(Long id, AlerteBurnout alerteBurnoutDetails) {
        Optional<AlerteBurnout> optionalAlerte = alerteBurnoutRepository.findById(id);
        if (optionalAlerte.isPresent()) {
            AlerteBurnout alerte = optionalAlerte.get();
            alerte.setUtilisateurId(alerteBurnoutDetails.getUtilisateurId());
            alerte.setNiveauRisque(alerteBurnoutDetails.getNiveauRisque());
            alerte.setDeclencheur(alerteBurnoutDetails.getDeclencheur());
            alerte.setDate(alerteBurnoutDetails.getDate());
            alerte.setTraitee(alerteBurnoutDetails.getTraitee());
            alerte.setSource(alerteBurnoutDetails.getSource());
            return alerteBurnoutRepository.save(alerte);
        }
        return null;
    }

    public void delete(Long id) {
        alerteBurnoutRepository.deleteById(id);
    }
}