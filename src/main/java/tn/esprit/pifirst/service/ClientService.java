// ClientService.java
package tn.esprit.pifirst.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pifirst.entity.Client;
import tn.esprit.pifirst.entity.User;
import tn.esprit.pifirst.enums.TypeClient;
import tn.esprit.pifirst.repository.ClientRepository;
import tn.esprit.pifirst.repository.UserRepository;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public List<Client> getByType(TypeClient type) {
        return clientRepository.findByType(type);
    }

    @Transactional
    public Client create(Client client, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        entityManager.createNativeQuery(
                        "INSERT INTO clients (id_user, type, nom_entreprise, secteur_activite, description, site_web) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, user.getId())
                .setParameter(2, client.getType() != null ? client.getType().name() : null)
                .setParameter(3, client.getNomEntreprise())
                .setParameter(4, client.getSecteurActivite())
                .setParameter(5, client.getDescription())
                .setParameter(6, client.getSiteWeb())
                .executeUpdate();

        client.setId(user.getId());
        client.setEmail(user.getEmail());
        client.setNom(user.getNom());
        client.setPrenom(user.getPrenom());
        return client;
    }

    public Client update(Long id, Client updated) {
        Client existing = getById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setPhoto(updated.getPhoto());
        existing.setType(updated.getType());
        existing.setNomEntreprise(updated.getNomEntreprise());
        existing.setSecteurActivite(updated.getSecteurActivite());
        existing.setDescription(updated.getDescription());
        existing.setSiteWeb(updated.getSiteWeb());
        return clientRepository.save(existing);
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}