package org.example.backend_pi.controller;
// controller/MachineController.java — CORRIGÉ: @Autowired ajouté pour notificationService

import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.NotificationType;
import org.example.backend_pi.service.MachineService;
import org.example.backend_pi.dto.MachineDTO;
import org.example.backend_pi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/machines")
public class MachineController {

    @Autowired
    private MachineService machineService;

    // ✅ BUG 1 CORRIGÉ : @Autowired manquant → NullPointerException lors approve/reject
    @Autowired
    private NotificationService notificationService;

    // ─── CRUD ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Machine> addMachine(@RequestBody MachineDTO machineDTO) {
        try {
            Machine machine = machineService.addMachine(machineDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(machine);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @RequestBody MachineDTO machineDTO) {
        Machine updated = machineService.updateMachine(id, machineDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMachine(@PathVariable Long id) {
        try {
            machineService.deleteMachine(id);
            return ResponseEntity.ok("Machine supprimée définitivement avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MachineDTO> getMachineById(@PathVariable Long id) {
        Machine machine = machineService.getMachineById(id);
        if (machine == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(convertToDTO(machine));
    }

    // ─── LISTES ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<MachineDTO>> getAllMachines() {
        List<Machine> machines = machineService.getAllMachines();
        return ResponseEntity.ok(machines.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    @GetMapping("/public")
    public ResponseEntity<List<MachineDTO>> getPublicMachines() {
        List<Machine> machines = machineService.getApprovedMachines();
        List<MachineDTO> dtos = machines.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsInApp()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<MachineDTO>> getMachinesBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(
                machineService.getMachinesBySupplier(supplierId).stream()
                        .map(this::convertToDTO).collect(Collectors.toList())
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MachineDTO>> getMachinesByCategory(@PathVariable String category) {
        try {
            CategoryType cat = CategoryType.valueOf(category.toUpperCase());
            return ResponseEntity.ok(
                    machineService.getMachinesByCategory(cat).stream()
                            .map(this::convertToDTO).collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<MachineDTO>> searchMachines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            CategoryType cat = null;
            if (category != null && !category.isEmpty())
                cat = CategoryType.valueOf(category.toUpperCase());
            return ResponseEntity.ok(
                    machineService.searchWithFilters(keyword, cat, location, minPrice, maxPrice)
                            .stream().map(this::convertToDTO).collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<MachineDTO>> getAvailableMachines() {
        return ResponseEntity.ok(
                machineService.getAvailableMachines().stream()
                        .map(this::convertToDTO).collect(Collectors.toList())
        );
    }

    @GetMapping("/type/{machineType}")
    public ResponseEntity<List<MachineDTO>> getMachinesByType(@PathVariable String machineType) {
        try {
            return ResponseEntity.ok(
                    machineService.getMachinesByType(machineType).stream()
                            .map(this::convertToDTO).collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── VALIDATION (Admin) ──────────────────────────────────────

    @PatchMapping("/{id}/approve")
    public ResponseEntity<MachineDTO> approveMachine(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        try {
            Machine machine = machineService.approveMachine(id, adminId != null ? adminId : 1L);

            // ✅ Maintenant @Autowired est présent → ne crash plus
            notificationService.create(
                    machine.getSupplierId(),
                    NotificationType.MACHINE_APPROVED,
                    "✅ Votre machine a été approuvée",
                    "Votre machine \"" + machine.getName() + "\" est maintenant visible par tous les utilisateurs.",
                    machine.getId(), "MACHINE",
                    "/machines/" + machine.getId()
            );

            return ResponseEntity.ok(convertToDTO(machine));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<MachineDTO> rejectMachine(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        try {
            String reason = payload.getOrDefault("reason", "");
            Machine machine = machineService.rejectMachine(id, reason, adminId != null ? adminId : 1L);

            notificationService.create(
                    machine.getSupplierId(),
                    NotificationType.MACHINE_REJECTED,
                    "❌ Votre machine a été rejetée",
                    "Votre machine \"" + machine.getName() + "\" a été rejetée."
                            + (reason.isEmpty() ? "" : " Raison : " + reason),
                    machine.getId(), "MACHINE",
                    "/machines/edit/" + machine.getId()
            );

            return ResponseEntity.ok(convertToDTO(machine));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MachineDTO>> getPendingMachines() {
        return ResponseEntity.ok(
                machineService.getPendingMachines().stream()
                        .map(this::convertToDTO).collect(Collectors.toList())
        );
    }

    @GetMapping("/approved")
    public ResponseEntity<List<MachineDTO>> getApprovedMachines() {
        return ResponseEntity.ok(
                machineService.getApprovedMachines().stream()
                        .map(this::convertToDTO).collect(Collectors.toList())
        );
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<MachineDTO>> getRejectedMachines() {
        return ResponseEntity.ok(
                machineService.getRejectedMachines().stream()
                        .map(this::convertToDTO).collect(Collectors.toList())
        );
    }

    // ─── CONVERSION ─────────────────────────────────────────────

    private MachineDTO convertToDTO(Machine machine) {
        MachineDTO dto = new MachineDTO();
        dto.setId(machine.getId());
        dto.setName(machine.getName());
        dto.setDescription(machine.getDescription());
        dto.setCategory(machine.getCategory() != null ? machine.getCategory().toString() : null);
        dto.setType(machine.getType() != null ? machine.getType().toString() : null);
        dto.setAvailability(machine.getAvailability() != null ? machine.getAvailability().toString() : null);
        dto.setTransactionType(machine.getTransactionType() != null ? machine.getTransactionType().toString() : null);
        dto.setPrice(machine.getPrice());
        dto.setPriceUnit(machine.getPriceUnit());
        dto.setLocation(machine.getLocation());
        dto.setContactInfo(machine.getContactInfo());
        dto.setStockQuantity(machine.getStockQuantity());
        dto.setImageUrls(machine.getImageUrls());
        dto.setSupplierId(machine.getSupplierId());
        dto.setSupplierName(machine.getSupplierName());
        dto.setSupplierCompanyName(machine.getSupplierCompanyName());
        dto.setIsInApp(machine.getIsInApp());
        dto.setValidationStatus(machine.getValidationStatus());
        dto.setSubCategory(machine.getSubCategory());
        dto.setBusinessType(machine.getBusinessType() != null ? machine.getBusinessType().name() : null);
        dto.setRejectionReason(machine.getRejectionReason());
        dto.setRejectedAt(machine.getRejectedAt());
        dto.setApprovedAt(machine.getApprovedAt());
        dto.setValidatedBy(machine.getValidatedBy());
        return dto;
    }
}