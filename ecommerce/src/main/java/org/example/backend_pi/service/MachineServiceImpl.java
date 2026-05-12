package org.example.backend_pi.service;
// service/impl/MachineServiceImpl.java
// ✅ CORRECTION : ContentModerationService ajouté dans addMachine() et updateMachine()
//    (identique à ce qui est déjà fait dans ServiceServiceImpl)

import org.example.backend_pi.dto.MachineDTO;
import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.MachineType;
import org.example.backend_pi.enums.TransactionType;
import org.example.backend_pi.repository.MachineRepository;
import org.example.backend_pi.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MachineServiceImpl implements MachineService {

    @Autowired
    private MachineRepository machineRepository;

    // ✅ AJOUT : injection du service de modération (manquait dans l'ancienne version)
    @Autowired
    private ContentModerationService moderationService;

    // ─── CRÉER ───────────────────────────────────────────────────

    @Override
    public Machine addMachine(MachineDTO dto) {
        // ✅ ÉTAPE 1 : modération IA AVANT la sauvegarde
        ContentModerationService.ModerationResult modResult =
                moderationService.moderate(
                        dto.getName(),
                        dto.getDescription(),
                        "MACHINE",
                        null
                );

        Machine machine = new Machine();
        mapDtoToEntity(dto, machine);

        // ✅ ÉTAPE 2 : définir le statut selon le résultat de la modération
        if (!modResult.approved) {
            machine.setValidationStatus("AUTO_REJECTED");
            machine.setRejectionReason(modResult.reason);
            machine.setRejectedAt(LocalDateTime.now());
        } else {
            machine.setValidationStatus("PENDING");
        }

        return machineRepository.save(machine);
    }

    // ─── METTRE À JOUR ───────────────────────────────────────────

    @Override
    public Machine updateMachine(Long id, MachineDTO dto) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée : " + id));

        mapDtoToEntity(dto, machine);

        // ✅ Re-modérer si la machine était AUTO_REJECTED (le fournisseur corrige son contenu)
        if ("AUTO_REJECTED".equals(machine.getValidationStatus())) {
            ContentModerationService.ModerationResult recheck =
                    moderationService.moderate(
                            dto.getName(),
                            dto.getDescription(),
                            "MACHINE",
                            id
                    );
            if (recheck.approved) {
                // Contenu corrigé → repasser en PENDING pour validation admin
                machine.setValidationStatus("PENDING");
                machine.setRejectionReason(null);
                machine.setRejectedAt(null);
            } else {
                // Toujours du contenu problématique
                machine.setValidationStatus("AUTO_REJECTED");
                machine.setRejectionReason(recheck.reason);
                machine.setRejectedAt(LocalDateTime.now());
            }
        } else {
            // Modification normale (PENDING ou REJECTED admin) → repasser en PENDING
            machine.setValidationStatus("PENDING");
            machine.setRejectionReason(null);
            machine.setRejectedAt(null);
            machine.setApprovedAt(null);
        }

        return machineRepository.save(machine);
    }

    // ─── MAPPING DTO → ENTITÉ ─────────────────────────────────────

    private void mapDtoToEntity(MachineDTO dto, Machine machine) {
        machine.setName(dto.getName());
        machine.setDescription(dto.getDescription());

        if (dto.getCategory() != null)
            machine.setCategory(CategoryType.valueOf(dto.getCategory().toUpperCase()));
        if (dto.getType() != null)
            machine.setType(MachineType.valueOf(dto.getType().toUpperCase()));
        if (dto.getAvailability() != null)
            machine.setAvailability(AvailabilityStatus.valueOf(dto.getAvailability().toUpperCase()));
        if (dto.getTransactionType() != null)
            machine.setTransactionType(TransactionType.valueOf(dto.getTransactionType().toUpperCase()));

        machine.setPrice(dto.getPrice());
        machine.setPriceUnit(dto.getPriceUnit());
        machine.setLocation(dto.getLocation());
        machine.setContactInfo(dto.getContactInfo());
        machine.setStockQuantity(dto.getStockQuantity());
        machine.setImageUrls(dto.getImageUrls());
        machine.setSupplierId(dto.getSupplierId());
        machine.setSupplierName(dto.getSupplierName());
        machine.setSupplierCompanyName(dto.getSupplierCompanyName());
        machine.setIsInApp(dto.getIsInApp() != null ? dto.getIsInApp() : true);
        machine.setSubCategory(dto.getSubCategory());

        if (dto.getBusinessType() != null && !dto.getBusinessType().isBlank()) {
            try {
                machine.setBusinessType(
                        org.example.backend_pi.enums.BusinessType.valueOf(
                                dto.getBusinessType().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // ─── SUPPRIMER ───────────────────────────────────────────────

    @Override
    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id))
            throw new RuntimeException("Machine non trouvée : " + id);
        machineRepository.deleteById(id);
    }

    // ─── LECTURE ─────────────────────────────────────────────────

    @Override
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    @Override
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée : " + id));
    }

    @Override
    public List<Machine> getMachinesBySupplier(Long supplierId) {
        return machineRepository.findBySupplierId(supplierId);
    }

    @Override
    public List<Machine> getMachinesByCategory(CategoryType category) {
        return machineRepository.findByCategory(category);
    }

    @Override
    public List<Machine> searchWithFilters(String keyword, CategoryType category,
                                           String location, Double minPrice, Double maxPrice) {
        return machineRepository.searchWithFilters(keyword, category, location, minPrice, maxPrice);
    }

    @Override
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByAvailability(AvailabilityStatus.AVAILABLE);
    }

    @Override
    public List<Machine> getMachinesByType(String machineType) {
        return machineRepository.findByType(MachineType.valueOf(machineType.toUpperCase()));
    }

    // ─── VALIDATION ADMIN ────────────────────────────────────────

    @Override
    public Machine approveMachine(Long id, Long adminId) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée : " + id));
        machine.setValidationStatus("APPROVED");
        machine.setApprovedAt(LocalDateTime.now());
        machine.setValidatedBy(adminId);
        machine.setRejectionReason(null);
        machine.setRejectedAt(null);
        return machineRepository.save(machine);
    }

    @Override
    public Machine rejectMachine(Long id, String reason, Long adminId) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée : " + id));
        machine.setValidationStatus("REJECTED");
        machine.setRejectionReason(reason);
        machine.setRejectedAt(LocalDateTime.now());
        machine.setValidatedBy(adminId);
        machine.setApprovedAt(null);
        return machineRepository.save(machine);
    }

    @Override
    public List<Machine> getPendingMachines() {
        return machineRepository.findByValidationStatus("PENDING");
    }

    @Override
    public List<Machine> getApprovedMachines() {
        return machineRepository.findByValidationStatus("APPROVED");
    }

    @Override
    public List<Machine> getRejectedMachines() {
        return machineRepository.findByValidationStatus("REJECTED");
    }
}