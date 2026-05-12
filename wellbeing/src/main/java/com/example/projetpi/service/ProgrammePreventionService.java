package com.example.projetpi.service;

import com.example.projetpi.entity.ProgrammePrevention;
import com.example.projetpi.repository.ProgrammePreventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProgrammePreventionService {

    @Autowired
    private ProgrammePreventionRepository programmePreventionRepository;

    public ProgrammePrevention create(ProgrammePrevention programmePrevention) {
        return programmePreventionRepository.save(programmePrevention);
    }

    public List<ProgrammePrevention> findAll() {
        return programmePreventionRepository.findAll();
    }

    public Optional<ProgrammePrevention> findById(Long id) {
        return programmePreventionRepository.findById(id);
    }

    public ProgrammePrevention update(Long id, ProgrammePrevention details) {
        Optional<ProgrammePrevention> optional = programmePreventionRepository.findById(id);
        if (optional.isPresent()) {
            ProgrammePrevention programme = optional.get();
            programme.setUtilisateurId(details.getUtilisateurId());
            programme.setNom(details.getNom());
            programme.setObjectif(details.getObjectif());
            programme.setDureesemaines(details.getDureesemaines());
            programme.setProgression(details.getProgression());
            programme.setActif(details.getActif());
            return programmePreventionRepository.save(programme);
        }
        return null;
    }

    public void delete(Long id) {
        programmePreventionRepository.deleteById(id);
    }
}