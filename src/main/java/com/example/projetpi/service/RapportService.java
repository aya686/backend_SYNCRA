package com.example.projetpi.service;

import com.example.projetpi.entity.Rapport;
import com.example.projetpi.repository.RapportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RapportService {

    @Autowired
    private RapportRepository rapportRepository;

    public Rapport create(Rapport rapport) {
        return rapportRepository.save(rapport);
    }

    public List<Rapport> findAll() {
        return rapportRepository.findAll();
    }

    public Optional<Rapport> findById(Long id) {
        return rapportRepository.findById(id);
    }

    public Rapport update(Long id, Rapport details) {
        Optional<Rapport> optional = rapportRepository.findById(id);
        if (optional.isPresent()) {
            Rapport rapport = optional.get();
            rapport.setContenu(details.getContenu());
            rapport.setRecommandations(details.getRecommandations());
            rapport.setDateRedaction(details.getDateRedaction());
            rapport.setConfidentiel(details.getConfidentiel());
            rapport.setSuiviPsycho(details.getSuiviPsycho());
            return rapportRepository.save(rapport);
        }
        return null;
    }

    public void delete(Long id) {
        rapportRepository.deleteById(id);
    }
}