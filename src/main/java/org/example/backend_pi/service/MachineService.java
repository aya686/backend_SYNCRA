package org.example.backend_pi.service;

// service/MachineService.java


import org.example.backend_pi.dto.MachineDTO;
import org.example.backend_pi.entity.Machine;
import org.example.backend_pi.enums.CategoryType;

import java.util.List;

public interface MachineService {

    Machine addMachine(MachineDTO machineDTO);

    Machine updateMachine(Long id, MachineDTO machineDTO);

    void deleteMachine(Long id);

    List<Machine> getAllMachines();

    Machine getMachineById(Long id);

    List<Machine> getMachinesBySupplier(Long supplierId);
    List<Machine> getMachinesByCategory(CategoryType category);
    List<Machine> searchWithFilters(String keyword, CategoryType category, String location, Double minPrice, Double maxPrice);

    List<Machine> getAvailableMachines();

    List<Machine> getMachinesByType(String machineType);
}