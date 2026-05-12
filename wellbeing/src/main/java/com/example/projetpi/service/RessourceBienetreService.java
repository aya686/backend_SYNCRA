package com.example.projetpi.service;
import com.example.projetpi.entity.RessourceBienetre;
import com.example.projetpi.repository.RessourceBienetreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class RessourceBienetreService {
    @Autowired
    private RessourceBienetreRepository ressourceBienetreRepository;
    public RessourceBienetre create(RessourceBienetre ressourceBienetre) {
        return ressourceBienetreRepository.save(ressourceBienetre);
    }
    public List<RessourceBienetre> findAll() {
        return ressourceBienetreRepository.findAll();
    }
    public Optional<RessourceBienetre> findById(Long id) {
        return ressourceBienetreRepository.findById(id);
    }
    public RessourceBienetre update(Long id, RessourceBienetre ressourceBienetreDetails) {
        Optional<RessourceBienetre> optionalRessource = ressourceBienetreRepository.findById(id);
        if (optionalRessource.isPresent()) {
            RessourceBienetre ressource = optionalRessource.get();
            ressource.setTitre(ressourceBienetreDetails.getTitre());
            ressource.setType(ressourceBienetreDetails.getType());
            ressource.setDescription(ressourceBienetreDetails.getDescription());
            ressource.setUrl(ressourceBienetreDetails.getUrl());
            ressource.setGratuit(ressourceBienetreDetails.getGratuit());
            ressource.setNiveau(ressourceBienetreDetails.getNiveau());
            return ressourceBienetreRepository.save(ressource);
        }
        return null;
    }
    public void delete(Long id) {
        ressourceBienetreRepository.deleteById(id);
    }
}
