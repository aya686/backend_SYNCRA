package com.example.projetpi.service;

import com.example.projetpi.entity.Antecedent;
import com.example.projetpi.repository.AntecedentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AntecedentService {

    @Autowired
    private AntecedentRepository antecedentRepository;

    public Antecedent create(Antecedent antecedent) {
        return antecedentRepository.save(antecedent);
    }

    public List<Antecedent> findAll() {
        return antecedentRepository.findAll();
    }

    public Optional<Antecedent> findById(Long id) {
        return antecedentRepository.findById(id);
    }

    public Antecedent update(Long id, Antecedent antecedentDetails) {
        Optional<Antecedent> optionalAntecedent = antecedentRepository.findById(id);
        if (optionalAntecedent.isPresent()) {
            Antecedent antecedent = optionalAntecedent.get();
            if (antecedentDetails.getType() != null) {
                antecedent.setType(antecedentDetails.getType());
            }
            if (antecedentDetails.getDescription() != null) {
                antecedent.setDescription(antecedentDetails.getDescription());
            }
            return antecedentRepository.save(antecedent);
        }
        return null;
    }

    public void delete(Long id) {
        antecedentRepository.deleteById(id);
    }
}
