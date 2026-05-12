package tn.esprit.pifirst.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pifirst.entity.Client;
import tn.esprit.pifirst.enums.TypeClient;
import tn.esprit.pifirst.service.ClientService;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    public List<Client> getAll() {
        return clientService.getAll();
    }

    @GetMapping("/{id}")
    public Client getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @GetMapping("/type/{type}")
    public List<Client> getByType(@PathVariable TypeClient type) {
        return clientService.getByType(type);
    }

    @PostMapping("/user/{idUser}")
    public Client create(@PathVariable Long idUser, @RequestBody Client client) {
        return clientService.create(client, idUser);
    }

    @PutMapping("/{id}")
    public Client update(@PathVariable Long id, @RequestBody Client client) {
        return clientService.update(id, client);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}