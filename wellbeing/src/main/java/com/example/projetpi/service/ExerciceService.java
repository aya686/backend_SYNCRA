package com.example.projetpi.service;
import com.example.projetpi.entity.Exercice;
import com.example.projetpi.repository.ExerciceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class ExerciceService {
    @Autowired
    private ExerciceRepository exerciceRepository;
    public Exercice create(Exercice exercice) {
        return exerciceRepository.save(exercice);
    }
    public List<Exercice> findAll() {
        return exerciceRepository.findAll();
    }
    public Optional<Exercice> findById(Long id) {
        return exerciceRepository.findById(id);
    }
    public Exercice update(Long id, Exercice exerciceDetails) {
        Optional<Exercice> optionalExercice = exerciceRepository.findById(id);
        if (optionalExercice.isPresent()) {
            Exercice exercice = optionalExercice.get();
            exercice.setNom(exerciceDetails.getNom());
            exercice.setDureeMinutes(exerciceDetails.getDureeMinutes());
            exercice.setFrequence(exerciceDetails.getFrequence());
            exercice.setInstructions(exerciceDetails.getInstructions());
            return exerciceRepository.save(exercice);
        }
        return null;
    }
    public void delete(Long id) {
        exerciceRepository.deleteById(id);
    }
}
