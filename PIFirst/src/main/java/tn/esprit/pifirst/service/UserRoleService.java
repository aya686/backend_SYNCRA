package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Role;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.entity.UserRole;
import tn.esprit.pifirst.enums.StatutRole;
import tn.esprit.pifirst.repository.RoleRepository;
import tn.esprit.pifirst.repository.UserRepository;
import tn.esprit.pifirst.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<UserRole> getAll() {
        return userRoleRepository.findAll();
    }

    public UserRole getById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserRole non trouvé"));
    }

    public List<UserRole> getByUser(Long idUser) {
        return userRoleRepository.findByUserId(idUser);
    }

    public List<UserRole> getRolesActifs(Long idUser) {
        return userRoleRepository.findByUserIdAndStatut(idUser, StatutRole.ACTIF);
    }

    public UserRole assignerRole(Long idUser, Long idRole, String motif, LocalDateTime dateExpiration) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        Role role = roleRepository.findById(idRole)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));

        userRoleRepository.findByUserIdAndRoleIdAndStatut(idUser, idRole, StatutRole.ACTIF)
                .ifPresent(r -> {
                    throw new RuntimeException("Ce rôle est déjà actif pour cet utilisateur");
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setStatut(StatutRole.ACTIF);
        userRole.setDateAttribution(LocalDateTime.now());
        userRole.setMotifChangement(motif);
        userRole.setDateExpiration(dateExpiration);

        return userRoleRepository.save(userRole);
    }

    public UserRole revoquerRole(Long id, String motif) {
        UserRole userRole = getById(id);
        userRole.setStatut(StatutRole.REVOQUE);
        userRole.setDateExpiration(LocalDateTime.now());
        userRole.setMotifChangement(motif);
        return userRoleRepository.save(userRole);
    }

    public void delete(Long id) {
        userRoleRepository.deleteById(id);
    }




}