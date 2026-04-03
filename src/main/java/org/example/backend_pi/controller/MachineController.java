package org.example.backend_pi.controller;
// controller/MachineController.java

import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.service.MachineService;
import org.example.backend_pi.dto.MachineDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    @Autowired
    private MachineService machineService;

    // Ajouter une machine
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

    // Modifier une machine
    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @RequestBody MachineDTO machineDTO) {
        try {
            Machine machine = machineService.updateMachine(id, machineDTO);
            return ResponseEntity.ok(machine);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
//supp
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

    // Récupérer toutes les machines
    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        List<Machine> machines = machineService.getAllMachines();
        return ResponseEntity.ok(machines);
    }

    // Récupérer une machine par ID
    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        Machine machine = machineService.getMachineById(id);
        if (machine == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(machine);
    }

    // Récupérer les machines d'un fournisseur
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Machine>> getMachinesBySupplier(@PathVariable Long supplierId) {
        List<Machine> machines = machineService.getMachinesBySupplier(supplierId);
        return ResponseEntity.ok(machines);
    }
    //GET http://localhost:8081/api/machines/category/AGRICOLE
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Machine>> getMachinesByCategory(@PathVariable String category) {
        try {
            CategoryType categoryType = CategoryType.valueOf(category.toUpperCase());
            List<Machine> machines = machineService.getMachinesByCategory(categoryType);
            return ResponseEntity.ok(machines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

    }
    //GET http://localhost:8081/api/machines/search?keyword=CNC&category=INDUSTRIELLE&location=Tunis&minPrice=50000&maxPrice=100000
    // Rechercher avec filtres
    @GetMapping("/search")
    public ResponseEntity<List<Machine>> searchMachines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        try {
            CategoryType categoryType = null;
            if (category != null && !category.isEmpty()) {
                categoryType = CategoryType.valueOf(category.toUpperCase());
            }
            List<Machine> machines = machineService.searchWithFilters(keyword, categoryType, location, minPrice, maxPrice);
            return ResponseEntity.ok(machines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
//GET http://localhost:8081/api/machines/available
    // Récupérer les machines disponibles
    @GetMapping("/available")
    public ResponseEntity<List<Machine>> getAvailableMachines() {
        List<Machine> machines = machineService.getAvailableMachines();
        return ResponseEntity.ok(machines);
    }
//GET http://localhost:8081/api/machines/type/EQUIPMENT
    // Récupérer les machines par type (EQUIPMENT, RAW_MATERIAL, PART_ACCESSORY)
    @GetMapping("/type/{machineType}")
    public ResponseEntity<List<Machine>> getMachinesByType(@PathVariable String machineType) {
        try {
            List<Machine> machines = machineService.getMachinesByType(machineType);
            return ResponseEntity.ok(machines);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}