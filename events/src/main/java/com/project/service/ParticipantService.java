package com.project.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.entity.Participant;
import com.project.repository.ParticipantRepository;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository repo;

    public List<Participant> getAll() {
        return repo.findAll();
    }

    public Participant save(Participant participant) {
        return repo.save(participant);
    }

    public Participant getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}