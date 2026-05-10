package com.example.projetpi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "specialiste")
public class Specialiste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String numeroOrdre;
    private String disponibilite;
    private Double tarif;

    @JsonIgnore
    @OneToMany(mappedBy = "specialiste", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SuiviPsycho> suiviPsychos;

    public Specialiste() {}

    public Specialiste(Long id, String type, String numeroOrdre, String disponibilite, Double tarif, List<SuiviPsycho> suiviPsychos) {
        this.id = id;
        this.type = type;
        this.numeroOrdre = numeroOrdre;
        this.disponibilite = disponibilite;
        this.tarif = tarif;
        this.suiviPsychos = suiviPsychos;
    }

    // Getters
    public Long getId() { return id; }
    public String getType() { return type; }
    public String getNumeroOrdre() { return numeroOrdre; }
    public String getDisponibilite() { return disponibilite; }
    public Double getTarif() { return tarif; }
    public List<SuiviPsycho> getSuiviPsychos() { return suiviPsychos; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }
    public void setDisponibilite(String disponibilite) { this.disponibilite = disponibilite; }
    public void setTarif(Double tarif) { this.tarif = tarif; }
    public void setSuiviPsychos(List<SuiviPsycho> suiviPsychos) { this.suiviPsychos = suiviPsychos; }
}