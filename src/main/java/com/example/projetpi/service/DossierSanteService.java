package com.example.projetpi.service;

import com.example.projetpi.entity.DossierSante;
import com.example.projetpi.repository.DossierSanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DossierSanteService {

    @Autowired
    private DossierSanteRepository dossierSanteRepository;

    public DossierSante create(DossierSante dossierSante) {
        return dossierSanteRepository.save(dossierSante);
    }

    public List<DossierSante> findAll() {
        return dossierSanteRepository.findAll();
    }

    public Optional<DossierSante> findById(Long id) {
        return dossierSanteRepository.findById(id);
    }

    @Transactional
    public DossierSante update(Long id, DossierSante dossierSanteDetails) {
        Optional<DossierSante> optionalDossierSante = dossierSanteRepository.findById(id);
        if (optionalDossierSante.isPresent()) {
            DossierSante existingDossier = optionalDossierSante.get();

            // Mettre à jour les champs simples
            existingDossier.setUtilisateurId(dossierSanteDetails.getUtilisateurId());
            existingDossier.setGroupeSanguin(dossierSanteDetails.getGroupeSanguin());
            existingDossier.setGenre(dossierSanteDetails.getGenre());
            existingDossier.setDateCreation(dossierSanteDetails.getDateCreation());

            // Mettre à jour les antécédents
            existingDossier.getAntecedents().clear();
            if (dossierSanteDetails.getAntecedents() != null) {
                dossierSanteDetails.getAntecedents().forEach(ant -> {
                    ant.setDossierSante(existingDossier);
                    existingDossier.getAntecedents().add(ant);
                });
            }

            // Mettre à jour les consultations
            existingDossier.getConsultations().clear();
            if (dossierSanteDetails.getConsultations() != null) {
                dossierSanteDetails.getConsultations().forEach(cons -> {
                    cons.setDossierSante(existingDossier);
                    existingDossier.getConsultations().add(cons);
                });
            }

            // Mettre à jour les documents médicaux
            existingDossier.getDocumentsMedicaux().clear();
            if (dossierSanteDetails.getDocumentsMedicaux() != null) {
                dossierSanteDetails.getDocumentsMedicaux().forEach(doc -> {
                    doc.setDossierSante(existingDossier);
                    existingDossier.getDocumentsMedicaux().add(doc);
                });
            }

            return dossierSanteRepository.save(existingDossier);
        }
        return null;
    }

    public void delete(Long id) {
        dossierSanteRepository.deleteById(id);
    }
}