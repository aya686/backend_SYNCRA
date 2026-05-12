// src/main/java/com/project/entity/TrainingData.java
package com.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_data")
public class TrainingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_type", nullable = false)
    private String userType;

    @Column(name = "user_budget", nullable = false)
    private Double userBudget;

    @Column(name = "user_price_sensitivity")
    private Double userPriceSensitivity;

    @Column(name = "user_spontaneity")
    private Double userSpontaneity;

    @Column(name = "user_loyalty")
    private Double userLoyalty;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_price", nullable = false)
    private Double eventPrice;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_popularity")
    private Double eventPopularity;

    @Column(name = "did_choose", nullable = false)
    private Boolean didChoose;

    @Column(name = "interaction_type")
    private String interactionType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructeurs
    public TrainingData() {}

    public TrainingData(String userType, Double userBudget, Long eventId, Double eventPrice,
                        String eventType, Boolean didChoose) {
        this.userType = userType;
        this.userBudget = userBudget;
        this.eventId = eventId;
        this.eventPrice = eventPrice;
        this.eventType = eventType;
        this.didChoose = didChoose;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Double getUserBudget() { return userBudget; }
    public void setUserBudget(Double userBudget) { this.userBudget = userBudget; }

    public Double getUserPriceSensitivity() { return userPriceSensitivity; }
    public void setUserPriceSensitivity(Double userPriceSensitivity) { this.userPriceSensitivity = userPriceSensitivity; }

    public Double getUserSpontaneity() { return userSpontaneity; }
    public void setUserSpontaneity(Double userSpontaneity) { this.userSpontaneity = userSpontaneity; }

    public Double getUserLoyalty() { return userLoyalty; }
    public void setUserLoyalty(Double userLoyalty) { this.userLoyalty = userLoyalty; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Double getEventPrice() { return eventPrice; }
    public void setEventPrice(Double eventPrice) { this.eventPrice = eventPrice; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Double getEventPopularity() { return eventPopularity; }
    public void setEventPopularity(Double eventPopularity) { this.eventPopularity = eventPopularity; }

    public Boolean getDidChoose() { return didChoose; }
    public void setDidChoose(Boolean didChoose) { this.didChoose = didChoose; }

    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}