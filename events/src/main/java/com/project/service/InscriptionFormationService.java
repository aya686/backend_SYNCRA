// com/project/service/InscriptionFormationService.java
package com.project.service;

import com.project.entity.InscriptionFormation;
import com.project.repository.InscriptionFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscriptionFormationService {

    @Autowired
    private InscriptionFormationRepository repository;

    public List<InscriptionFormation> getAll() {
        return repository.findAll();
    }

    public InscriptionFormation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
    }

    public List<InscriptionFormation> getByParticipantId(Long participantId) {
        return repository.findByParticipantId(participantId);
    }

    public List<InscriptionFormation> getByFormationId(Long formationId) {
        return repository.findByFormationId(formationId);
    }

    public boolean isAlreadyInscribed(Long participantId, Long formationId) {
        return repository.existsByParticipantIdAndFormationId(participantId, formationId);
    }

    public InscriptionFormation updateProgression(Long id, Integer progression) {
        InscriptionFormation inscription = getById(id);
        inscription.setProgression(progression);
        if (progression >= 100) {
            inscription.setDateCompletion(LocalDateTime.now());
            inscription.setStatut("termine");
        }
        return repository.save(inscription);
    }

    public void deleteInscription(Long id) {
        repository.deleteById(id);
    }
}