package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.UserBadge;
import tn.esprit.pifirst.service.UserBadgeService;

import java.util.List;

@RestController
@RequestMapping("/api/user-badges")
public class UserBadgeController {

    @Autowired
    private UserBadgeService userBadgeService;

    @GetMapping("/user/{idUser}")
    public List<UserBadge> getByUser(@PathVariable Long idUser) {
        return userBadgeService.getByUser(idUser);
    }

    @PostMapping("/attribuer/{idUser}/{idBadge}")
    public UserBadge attribuerBadge(@PathVariable Long idUser,
                                    @PathVariable Long idBadge) {
        return userBadgeService.attribuerBadge(idUser, idBadge);
    }

    @PostMapping("/verifier/{idUser}")
    public void verifierBadges(@PathVariable Long idUser) {
        userBadgeService.verifierEtAttribuerBadges(idUser);
    }
}