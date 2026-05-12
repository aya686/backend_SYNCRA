package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.UserRole;
import tn.esprit.pifirst.service.UserRoleService;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public List<UserRole> getAll() {
        return userRoleService.getAll();
    }

    @GetMapping("/{id}")
    public UserRole getById(@PathVariable Long id) {
        return userRoleService.getById(id);
    }

    @GetMapping("/user/{idUser}")
    public List<UserRole> getByUser(@PathVariable Long idUser) {
        return userRoleService.getByUser(idUser);
    }

    @GetMapping("/user/{idUser}/actifs")
    public List<UserRole> getRolesActifs(@PathVariable Long idUser) {
        return userRoleService.getRolesActifs(idUser);
    }

    @PostMapping("/assigner/{idUser}")
    public UserRole assignerRole(@PathVariable Long idUser,
                                 @RequestParam Long idRole,
                                 @RequestParam(required = false) String motif,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateExpiration) {
        return userRoleService.assignerRole(idUser, idRole, motif, dateExpiration);
    }

    @PutMapping("/{id}/revoquer")
    public UserRole revoquerRole(@PathVariable Long id,
                                 @RequestParam(required = false) String motif) {
        return userRoleService.revoquerRole(id, motif);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRoleService.delete(id);
    }
}