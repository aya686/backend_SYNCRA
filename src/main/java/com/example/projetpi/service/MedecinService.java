package com.example.projetpi.service;

import com.example.projetpi.entity.Medecin;
import com.example.projetpi.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;

    public Medecin create(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }

    public Optional<Medecin> findById(Long id) {
        return medecinRepository.findById(id);
    }

    public Medecin update(Long id, Medecin medecinDetails) {
        Optional<Medecin> optionalMedecin = medecinRepository.findById(id);
        if (optionalMedecin.isPresent()) {
            Medecin medecin = optionalMedecin.get();
            medecin.setNom(medecinDetails.getNom());
            medecin.setPrenom(medecinDetails.getPrenom());
            medecin.setSpecialite(medecinDetails.getSpecialite());
            medecin.setTelephone(medecinDetails.getTelephone());
            medecin.setAdresseCabinet(medecinDetails.getAdresseCabinet());
            return medecinRepository.save(medecin);
        }
        return null;
    }

    public void delete(Long id) {
        medecinRepository.deleteById(id);
    }
}
