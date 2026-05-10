package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suivi_psycho")
public class SuiviPsycho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate datSeance;
    private String type;
    private Integer duree;
    private String statut;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "specialiste_id")
    private Specialiste specialiste;

    public SuiviPsycho() {}

    public SuiviPsycho(Long id, LocalDate datSeance, String type, Integer duree, String statut, Specialiste specialiste) {
        this.id = id;
        this.datSeance = datSeance;
        this.type = type;
        this.duree = duree;
        this.statut = statut;
        this.specialiste = specialiste;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDate getDatSeance() { return datSeance; }
    public String getType() { return type; }
    public Integer getDuree() { return duree; }
    public String getStatut() { return statut; }
    public Specialiste getSpecialiste() { return specialiste; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDatSeance(LocalDate datSeance) { this.datSeance = datSeance; }
    public void setType(String type) { this.type = type; }
    public void setDuree(Integer duree) { this.duree = duree; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setSpecialiste(Specialiste specialiste) { this.specialiste = specialiste; }
}