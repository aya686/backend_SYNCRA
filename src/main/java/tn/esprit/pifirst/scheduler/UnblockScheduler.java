package tn.esprit.pifirst.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.enums.Statut;
import tn.esprit.pifirst.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class UnblockScheduler {

    @Autowired
    private UserRepository userRepository;

    @Scheduled(fixedDelay = 60000)  // Toutes les 60 secondes
    public void unblockExpiredAccounts() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        List<User> blockedUsers = userRepository.findByStatutAndBlockedAtBefore(Statut.SUSPENDU, fiveMinutesAgo);

        for (User user : blockedUsers) {
            user.setStatut(Statut.ACTIF);
            user.setBlockedAt(null);
            userRepository.save(user);
            System.out.println("✅ Compte réactivé automatiquement: " + user.getEmail());
        }
    }
}