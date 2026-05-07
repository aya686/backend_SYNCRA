// src/main/java/com/project/dto/TrainingDataDTO.java
package com.project.dto;

public class TrainingDataDTO {
    private String userType;
    private Double userBudget;
    private Double userPriceSensitivity;
    private Double userSpontaneity;
    private Double userLoyalty;
    private Long eventId;
    private Double eventPrice;
    private String eventType;
    private Double eventPopularity;
    private Boolean didChoose;
    private String interactionType;

    // Getters et Setters
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
}