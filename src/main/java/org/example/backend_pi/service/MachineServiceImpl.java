package org.example.backend_pi.service;
// service/impl/MachineServiceImpl.java

import org.example.backend_pi.dto.MachineDTO;
import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.AvailabilityStatus;
import org.example.backend_pi.enums.CategoryType;
import org.example.backend_pi.enums.MachineType;
import org.example.backend_pi.enums.TransactionType;
import org.example.backend_pi.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MachineServiceImpl implements MachineService {

    @Autowired
    private MachineRepository machineRepository;



    @Override
    public Machine addMachine(MachineDTO machineDTO) {
        Machine machine = new Machine();
        machine.setName(machineDTO.getName());
        machine.setDescription(machineDTO.getDescription());

        // Set enum values with null check
        if (machineDTO.getType() != null) {
            machine.setType(MachineType.valueOf(machineDTO.getType()));
        }

        if (machineDTO.getAvailability() != null) {
            machine.setAvailability(AvailabilityStatus.valueOf(machineDTO.getAvailability()));
        } else {
            machine.setAvailability(AvailabilityStatus.AVAILABLE);
        }

        if (machineDTO.getTransactionType() != null) {
            machine.setTransactionType(TransactionType.valueOf(machineDTO.getTransactionType()));
        }

        machine.setPrice(machineDTO.getPrice());
        machine.setPriceUnit(machineDTO.getPriceUnit());
        machine.setLocation(machineDTO.getLocation());
        machine.setContactInfo(machineDTO.getContactInfo());
        machine.setStockQuantity(machineDTO.getStockQuantity());
        machine.setImageUrls(machineDTO.getImageUrls());
        machine.setSupplierId(machineDTO.getSupplierId());
        machine.setSupplierName(machineDTO.getSupplierName());
        machine.setSupplierCompanyName(machineDTO.getSupplierCompanyName());
        machine.setIsInApp(machineDTO.getIsInApp() != null ? machineDTO.getIsInApp() : true);



        if (machineDTO.getCategory() != null) {
            machine.setCategory(CategoryType.valueOf(machineDTO.getCategory()));
        }



        return machineRepository.save(machine);
    }

    @Override
    public Machine updateMachine(Long id, MachineDTO machineDTO) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée avec l'ID: " + id));

        // Update only non-null fields
        if (machineDTO.getName() != null) machine.setName(machineDTO.getName());
        if (machineDTO.getDescription() != null) machine.setDescription(machineDTO.getDescription());
        if (machineDTO.getPrice() != null) machine.setPrice(machineDTO.getPrice());
        if (machineDTO.getPriceUnit() != null) machine.setPriceUnit(machineDTO.getPriceUnit());
        if (machineDTO.getLocation() != null) machine.setLocation(machineDTO.getLocation());
        if (machineDTO.getContactInfo() != null) machine.setContactInfo(machineDTO.getContactInfo());
        if (machineDTO.getStockQuantity() != null) machine.setStockQuantity(machineDTO.getStockQuantity());
        if (machineDTO.getImageUrls() != null) machine.setImageUrls(machineDTO.getImageUrls());

        if (machineDTO.getAvailability() != null) {
            machine.setAvailability(AvailabilityStatus.valueOf(machineDTO.getAvailability()));
        }

        if (machineDTO.getTransactionType() != null) {
            machine.setTransactionType(TransactionType.valueOf(machineDTO.getTransactionType()));
        }

        if (machineDTO.getCategory() != null) {
            machine.setCategory(CategoryType.valueOf(machineDTO.getCategory()));
        }

        machine.setUpdatedAt(LocalDateTime.now());

        return machineRepository.save(machine);
    }

    @Override
    @Transactional
    public void deleteMachine(Long id) {
        // Vérifier si la machine existe
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvée avec l'ID: " + id));

        // Suppression complète de la base de données
        machineRepository.delete(machine);
    }

    @Override
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();  // Récupère toutes les machines (pas de filtre isActive)
    }

    @Override
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id).orElse(null);
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
    public List<Machine> searchWithFilters(String keyword, CategoryType category, String location, Double minPrice, Double maxPrice) {
        return machineRepository.searchWithFilters(keyword, category, location, minPrice, maxPrice);
    }

    @Override
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByAvailability(AvailabilityStatus.AVAILABLE);
    }

    @Override
    public List<Machine> getMachinesByType(String machineType) {
        MachineType type = MachineType.valueOf(machineType.toUpperCase());
        return machineRepository.findByType(type);
    }
}