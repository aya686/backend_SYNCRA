package com.project.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.entity.Moduleformation;
import com.project.repository.ModuleformationRepository;

@Service
public class ModuleformationService {

    @Autowired
    private ModuleformationRepository repo;

    public List<Moduleformation> getAll() {
        return repo.findAll();
    }

    public Moduleformation save(Moduleformation module) {
        return repo.save(module);
    }

    public Moduleformation getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec id: " + id));
    }

    public Moduleformation update(Long id, Moduleformation moduleDetails) {
        Moduleformation module = getById(id);
        module.setTitre(moduleDetails.getTitre());
        module.setOrdre(moduleDetails.getOrdre());
        module.setDuree(moduleDetails.getDuree());
        module.setContenu(moduleDetails.getContenu());
        module.setFormation(moduleDetails.getFormation());
        return repo.save(module);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}