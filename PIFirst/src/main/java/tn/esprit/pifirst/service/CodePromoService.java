package tn.esprit.pifirst.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pifirst.entity.CodePromo;
import tn.esprit.pifirst.enums.StatutCodePromo;
import tn.esprit.pifirst.repository.CodePromoRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodePromoService {

    private final CodePromoRepository codePromoRepository;

    public List<CodePromo> getAll() {
        return codePromoRepository.findAll();
    }

    public CodePromo getById(Long id) {
        return codePromoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Code promo non trouvé"));
    }

    public CodePromo getByCode(String code) {
        return codePromoRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Code promo non trouvé"));
    }

    public CodePromo create(CodePromo codePromo) {
        if (codePromoRepository.findByCode(codePromo.getCode()).isPresent()) {
            throw new RuntimeException("Ce code promo existe déjà");
        }
        codePromo.setNbUtilisationsCourant(0);
        codePromo.setStatut(StatutCodePromo.ACTIF);
        return codePromoRepository.save(codePromo);
    }

    public CodePromo update(Long id, CodePromo updated) {
        CodePromo existing = getById(id);
        existing.setCode(updated.getCode());
        existing.setReductionPct(updated.getReductionPct());
        existing.setDateExpiration(updated.getDateExpiration());
        existing.setNbUtilisationsMax(updated.getNbUtilisationsMax());
        existing.setStatut(updated.getStatut());
        return codePromoRepository.save(existing);
    }

    public void delete(Long id) {
        codePromoRepository.deleteById(id);
    }

    // Fonctionnalité avancée : expiration automatique des codes
    public void verifierExpirations() {
        List<CodePromo> codes = codePromoRepository.findAll();
        for (CodePromo code : codes) {
            if (code.getStatut() == StatutCodePromo.ACTIF &&
                    code.getDateExpiration() != null &&
                    code.getDateExpiration().isBefore(LocalDateTime.now())) {
                code.setStatut(StatutCodePromo.EXPIRE);
                codePromoRepository.save(code);
            }
        }
    }
}