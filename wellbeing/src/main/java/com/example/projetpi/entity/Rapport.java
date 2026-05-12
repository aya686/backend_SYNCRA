package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rapport")
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenu;
    private String recommandations;
    private LocalDate dateRedaction;
    private Boolean confidentiel;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "suivi_id")
    private SuiviPsycho suiviPsycho;

    public Rapport() {}

    public Rapport(Long id, String contenu, String recommandations, LocalDate dateRedaction, Boolean confidentiel, SuiviPsycho suiviPsycho) {
        this.id = id;
        this.contenu = contenu;
        this.recommandations = recommandations;
        this.dateRedaction = dateRedaction;
        this.confidentiel = confidentiel;
        this.suiviPsycho = suiviPsycho;
    }

    // Getters
    public Long getId() { return id; }
    public String getContenu() { return contenu; }
    public String getRecommandations() { return recommandations; }
    public LocalDate getDateRedaction() { return dateRedaction; }
    public Boolean getConfidentiel() { return confidentiel; }
    public SuiviPsycho getSuiviPsycho() { return suiviPsycho; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }
    public void setDateRedaction(LocalDate dateRedaction) { this.dateRedaction = dateRedaction; }
    public void setConfidentiel(Boolean confidentiel) { this.confidentiel = confidentiel; }
    public void setSuiviPsycho(SuiviPsycho suiviPsycho) { this.suiviPsycho = suiviPsycho; }
}