// src/main/java/com/project/service/TrainingDataService.java
package com.project.service;

import com.project.dto.TrainingDataDTO;
import com.project.entity.TrainingData;
import com.project.repository.TrainingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingDataService {

    @Autowired
    private TrainingDataRepository repository;

    public TrainingData save(TrainingDataDTO dto) {
        TrainingData data = new TrainingData();
        data.setUserType(dto.getUserType());
        data.setUserBudget(dto.getUserBudget());
        data.setUserPriceSensitivity(dto.getUserPriceSensitivity());
        data.setUserSpontaneity(dto.getUserSpontaneity());
        data.setUserLoyalty(dto.getUserLoyalty());
        data.setEventId(dto.getEventId());
        data.setEventPrice(dto.getEventPrice());
        data.setEventType(dto.getEventType());
        data.setEventPopularity(dto.getEventPopularity());
        data.setDidChoose(dto.getDidChoose());
        data.setInteractionType(dto.getInteractionType());
        data.setCreatedAt(LocalDateTime.now());

        return repository.save(data);
    }

    public List<TrainingData> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<TrainingData> getRecentData() {
        return repository.findRecentData();
    }

    public long count() {
        return repository.count();
    }

    public long countPositiveChoices() {
        return repository.findAll().stream().filter(TrainingData::getDidChoose).count();
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    // Récupérer les données formatées pour TensorFlow.js
    public List<TrainingData> getTrainingDataForAI() {
        return repository.findAll();
    }
}