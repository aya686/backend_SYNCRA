package com.project.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.entity.Session;
import com.project.repository.SessionRepository;

@Service
public class SessionService {

    @Autowired
    private SessionRepository repo;

    public List<Session> getAll() {
        return repo.findAll();
    }

    // AJOUTEZ CETTE MÉTHODE
    public List<Session> getByFormationId(Long formationId) {
        return repo.findByFormation_FormationId(formationId);
    }

    public Session save(Session session) {
        return repo.save(session);
    }

    public Session getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée avec id: " + id));
    }

    public Session update(Long id, Session sessionDetails) {
        Session session = getById(id);
        session.setTitre(sessionDetails.getTitre());
        session.setDateHeure(sessionDetails.getDateHeure());
        session.setDateFin(sessionDetails.getDateFin());
        session.setDuree(sessionDetails.getDuree());
        session.setIntervenant(sessionDetails.getIntervenant());
        session.setLieu(sessionDetails.getLieu());
        session.setCapaciteMax(sessionDetails.getCapaciteMax());
        session.setStatut(sessionDetails.getStatut());
        session.setFormation(sessionDetails.getFormation());
        session.setFormateur(sessionDetails.getFormateur());
        return repo.save(session);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}