package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pifirst.entity.Role;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.entity.UserRole;
import tn.esprit.pifirst.enums.Statut;
import tn.esprit.pifirst.service.RoleService;
import tn.esprit.pifirst.service.UserRoleService;
import tn.esprit.pifirst.service.UserService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        user.setDateInscription(LocalDateTime.now());
        user.setStatut(Statut.ACTIF);
        return userService.create(user);
    }

    // ✅ Endpoint pour créer un administrateur
    @PostMapping("/admin")
    public User createAdmin(@RequestBody User user) {
        user.setDateInscription(LocalDateTime.now());
        user.setStatut(Statut.ACTIF);
        User savedUser = userService.create(user);

        Role adminRole = roleService.getByLibelle("ADMIN");

        if (adminRole != null) {
            userRoleService.assignerRole(
                    savedUser.getId(),
                    adminRole.getId(),
                    "Création compte administrateur",
                    null
            );
            System.out.println("✅ Rôle ADMIN assigné à " + savedUser.getEmail());
        } else {
            System.out.println("⚠️ Rôle ADMIN non trouvé !");
        }

        return savedUser;
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @PutMapping("/{id}/revoquer")
    public UserRole revoquerRole(@PathVariable Long id,
                                 @RequestParam(required = false) String motif) {
        return userRoleService.revoquerRole(id, motif);
    }

    @PutMapping("/{id}/with-photo")
    public User updateWithPhoto(@PathVariable Long id,
                                @ModelAttribute User user,
                                @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {
        if (photo != null && !photo.isEmpty()) {
            String uploadDir = "uploads/photos/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            File dest = new File(uploadDir + fileName);
            photo.transferTo(dest);
            user.setPhoto("/uploads/photos/" + fileName);
        }
        return userService.update(id, user);
    }
}