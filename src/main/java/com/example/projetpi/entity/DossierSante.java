package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossier_sante")
public class DossierSante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;
    private String groupeSanguin;
    private String genre;
    private LocalDate dateCreation;

    @OneToMany(mappedBy = "dossierSante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Antecedent> antecedents = new ArrayList<>();

    @OneToMany(mappedBy = "dossierSante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "dossierSante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<DocumentMedical> documentsMedicaux = new ArrayList<>();

    // Constructeurs
    public DossierSante() {}

    public DossierSante(Long id, Long utilisateurId, String groupeSanguin, String genre, LocalDate dateCreation) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.groupeSanguin = groupeSanguin;
        this.genre = genre;
        this.dateCreation = dateCreation;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public String getGroupeSanguin() { return groupeSanguin; }
    public String getGenre() { return genre; }
    public LocalDate getDateCreation() { return dateCreation; }
    public List<Antecedent> getAntecedents() { return antecedents; }
    public List<Consultation> getConsultations() { return consultations; }
    public List<DocumentMedical> getDocumentsMedicaux() { return documentsMedicaux; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public void setAntecedents(List<Antecedent> antecedents) { this.antecedents = antecedents; }
    public void setConsultations(List<Consultation> consultations) { this.consultations = consultations; }
    public void setDocumentsMedicaux(List<DocumentMedical> documentsMedicaux) { this.documentsMedicaux = documentsMedicaux; }
}