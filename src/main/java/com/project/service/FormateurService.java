package com.project.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.entity.Formateur;
import com.project.repository.FormateurRepository;

@Service
public class FormateurService {

    @Autowired
    private FormateurRepository repo;

    public List<Formateur> getAll() {
        return repo.findAll();
    }

    public Formateur save(Formateur formateur) {
        return repo.save(formateur);
    }

    public Formateur getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec id: " + id));
    }

    public Formateur update(Long id, Formateur formateurDetails) {
        Formateur formateur = getById(id);
        formateur.setExpertise(formateurDetails.getExpertise());
        formateur.setBio(formateurDetails.getBio());
        formateur.setNoteMoyenne(formateurDetails.getNoteMoyenne());
        return repo.save(formateur);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}