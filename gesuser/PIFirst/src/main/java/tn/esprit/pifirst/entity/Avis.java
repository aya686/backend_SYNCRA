package tn.esprit.pifirst.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer note;
    private String commentaire;
    private String reponse;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "id_auteur")
    private User auteur;

    @ManyToOne
    @JoinColumn(name = "id_cible")
    private User cible;

    public Avis() {
    }

    public Avis(Long id, Integer note, String commentaire, String reponse, LocalDateTime date, User auteur, User cible) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.reponse = reponse;
        this.date = date;
        this.auteur = auteur;
        this.cible = cible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getAuteur() {
        return auteur;
    }

    public void setAuteur(User auteur) {
        this.auteur = auteur;
    }

    public User getCible() {
        return cible;
    }

    public void setCible(User cible) {
        this.cible = cible;
    }
}
