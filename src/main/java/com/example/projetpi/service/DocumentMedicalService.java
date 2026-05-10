package com.example.projetpi.service;

import com.example.projetpi.entity.DocumentMedical;
import com.example.projetpi.repository.DocumentMedicalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentMedicalService {

    @Autowired
    private DocumentMedicalRepository documentMedicalRepository;

    public DocumentMedical create(DocumentMedical documentMedical) {
        return documentMedicalRepository.save(documentMedical);
    }

    public List<DocumentMedical> findAll() {
        return documentMedicalRepository.findAll();
    }

    public Optional<DocumentMedical> findById(Long id) {
        return documentMedicalRepository.findById(id);
    }

    public DocumentMedical update(Long id, DocumentMedical documentMedicalDetails) {
        Optional<DocumentMedical> optionalDocumentMedical = documentMedicalRepository.findById(id);
        if (optionalDocumentMedical.isPresent()) {
            DocumentMedical documentMedical = optionalDocumentMedical.get();
            documentMedical.setNom(documentMedicalDetails.getNom());
            documentMedical.setType(documentMedicalDetails.getType());
            documentMedical.setUrl(documentMedicalDetails.getUrl());
            return documentMedicalRepository.save(documentMedical);
        }
        return null;
    }

    public void delete(Long id) {
        documentMedicalRepository.deleteById(id);
    }
}
