package com.example.projetpi.service;

import com.example.projetpi.entity.Consultation;
import com.example.projetpi.repository.ConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    public Consultation create(Consultation consultation) {
        return consultationRepository.save(consultation);
    }

    public List<Consultation> findAll() {
        return consultationRepository.findAll();
    }

    public Optional<Consultation> findById(Long id) {
        return consultationRepository.findById(id);
    }

    public Consultation update(Long id, Consultation consultationDetails) {
        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isPresent()) {
            Consultation consultation = optionalConsultation.get();
            consultation.setDate(consultationDetails.getDate());
            consultation.setHeure(consultationDetails.getHeure());
            consultation.setMotif(consultationDetails.getMotif());
            return consultationRepository.save(consultation);
        }
        return null;
    }

    public void delete(Long id) {
        consultationRepository.deleteById(id);
    }
}
