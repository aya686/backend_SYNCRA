package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Badge;
import tn.esprit.pifirst.repository.BadgeRepository;

import java.util.List;

@Service
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    public List<Badge> getAll() {
        return badgeRepository.findAll();
    }

    public Badge getById(Long id) {
        return badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));
    }

    public Badge create(Badge badge) {
        return badgeRepository.save(badge);
    }

    public Badge update(Long id, Badge updated) {
        Badge existing = getById(id);
        existing.setLibelle(updated.getLibelle());
        existing.setDescription(updated.getDescription());
        existing.setIcone(updated.getIcone());
        existing.setSeuilObtention(updated.getSeuilObtention());
        return badgeRepository.save(existing);
    }

    public void delete(Long id) {
        badgeRepository.deleteById(id);
    }
}
