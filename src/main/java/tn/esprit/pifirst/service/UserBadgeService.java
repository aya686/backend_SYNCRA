package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.Badge;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.entity.UserBadge;
import tn.esprit.pifirst.repository.AvisRepository;
import tn.esprit.pifirst.repository.BadgeRepository;
import tn.esprit.pifirst.repository.UserBadgeRepository;
import tn.esprit.pifirst.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserBadgeService {

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private AvisRepository avisRepository;

    public List<UserBadge> getByUser(Long idUser) {
        return userBadgeRepository.findByUserId(idUser);
    }

    public UserBadge attribuerBadge(Long idUser, Long idBadge) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        Badge badge = badgeRepository.findById(idBadge)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));

        // Vérifier si déjà attribué
        Optional<UserBadge> existing = userBadgeRepository
                .findByUserIdAndBadgeId(idUser, idBadge);
        if (existing.isPresent()) {
            throw new RuntimeException("Badge déjà attribué");
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setDateObtention(LocalDateTime.now());

        return userBadgeRepository.save(userBadge);
    }

    public void verifierEtAttribuerBadges(Long idUser) {
        Double score = avisRepository.findAvgNoteByCibleId(idUser);
        List<Badge> tousLesBadges = badgeRepository.findAll();

        for (Badge badge : tousLesBadges) {
            if (score != null && score >= badge.getSeuilObtention()) {
                Optional<UserBadge> existing = userBadgeRepository
                        .findByUserIdAndBadgeId(idUser, badge.getId());
                if (existing.isEmpty()) {
                    attribuerBadge(idUser, badge.getId());
                }
            }
        }
    }
}
