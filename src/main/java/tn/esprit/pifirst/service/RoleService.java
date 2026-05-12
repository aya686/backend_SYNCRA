package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Role;
import tn.esprit.pifirst.repository.RoleRepository;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }

    // AJOUTER CETTE MÉTHODE
    public Role getByLibelle(String libelle) {
        return roleRepository.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Rôle '" + libelle + "' non trouvé"));
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role update(Long id, Role updated) {
        Role existing = getById(id);
        existing.setLibelle(updated.getLibelle());
        existing.setDescription(updated.getDescription());
        return roleRepository.save(existing);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}