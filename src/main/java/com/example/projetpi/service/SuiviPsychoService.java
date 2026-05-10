package com.example.projetpi.service;

import com.example.projetpi.entity.SuiviPsycho;
import com.example.projetpi.repository.SuiviPsychoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SuiviPsychoService {

    @Autowired
    private SuiviPsychoRepository suiviPsychoRepository;

    public SuiviPsycho create(SuiviPsycho suiviPsycho) {
        return suiviPsychoRepository.save(suiviPsycho);
    }

    public List<SuiviPsycho> findAll() {
        return suiviPsychoRepository.findAll();
    }

    public Optional<SuiviPsycho> findById(Long id) {
        return suiviPsychoRepository.findById(id);
    }

    public SuiviPsycho update(Long id, SuiviPsycho details) {
        Optional<SuiviPsycho> optional = suiviPsychoRepository.findById(id);
        if (optional.isPresent()) {
            SuiviPsycho suivi = optional.get();
            suivi.setDatSeance(details.getDatSeance());
            suivi.setType(details.getType());
            suivi.setDuree(details.getDuree());
            suivi.setStatut(details.getStatut());
            suivi.setSpecialiste(details.getSpecialiste());
            return suiviPsychoRepository.save(suivi);
        }
        return null;
    }

    public void delete(Long id) {
        suiviPsychoRepository.deleteById(id);
    }
}