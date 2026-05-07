package com.project.dto;

public class InscriptionFormationRequest {
    private Long participantId;
    private Long formationId;
    private String statut;
    private Integer progression;
    private String email;
    private String nomParticipant;
    private String formationTitle;
    private String formationNiveau;
    private Integer formationDuree;
    private Double formationPrix;
    private String nom;
    private String prenom;
    private String telephone;
    private String message;
    private Long sessionId;


    // Constructeurs
    public InscriptionFormationRequest() {}

    // Getters et Setters
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public Long getFormationId() { return formationId; }
    public void setFormationId(Long formationId) { this.formationId = formationId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getProgression() { return progression; }
    public void setProgression(Integer progression) { this.progression = progression; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFormationTitle() { return formationTitle; }
    public void setFormationTitle(String formationTitle) { this.formationTitle = formationTitle; }

    public String getFormationNiveau() { return formationNiveau; }
    public void setFormationNiveau(String formationNiveau) { this.formationNiveau = formationNiveau; }

    public Integer getFormationDuree() { return formationDuree; }
    public void setFormationDuree(Integer formationDuree) { this.formationDuree = formationDuree; }

    public Double getFormationPrix() { return formationPrix; }
    public void setFormationPrix(Double formationPrix) { this.formationPrix = formationPrix; }
}
