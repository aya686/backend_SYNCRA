package com.example.projetpi.service;

import com.example.projetpi.entity.Specialiste;
import com.example.projetpi.repository.SpecialisteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SpecialisteService {

    @Autowired
    private SpecialisteRepository specialisteRepository;

    public Specialiste create(Specialiste specialiste) {
        return specialisteRepository.save(specialiste);
    }

    public List<Specialiste> findAll() {
        return specialisteRepository.findAll();
    }

    public Optional<Specialiste> findById(Long id) {
        return specialisteRepository.findById(id);
    }

    public Specialiste update(Long id, Specialiste details) {
        Optional<Specialiste> optional = specialisteRepository.findById(id);
        if (optional.isPresent()) {
            Specialiste specialiste = optional.get();
            specialiste.setType(details.getType());
            specialiste.setNumeroOrdre(details.getNumeroOrdre());
            specialiste.setDisponibilite(details.getDisponibilite());
            specialiste.setTarif(details.getTarif());
            return specialisteRepository.save(specialiste);
        }
        return null;
    }

    public void delete(Long id) {
        specialisteRepository.deleteById(id);
    }
}