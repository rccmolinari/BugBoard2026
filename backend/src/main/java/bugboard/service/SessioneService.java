package bugboard.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import bugboard.repository.SessioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Utente;
import bugboard.model.Sessione;

/*
 * DIP — implementa ISessioneService; tutti i consumer dipendono
 * dall'interfaccia, non da questa classe concreta.
 */
@Service
public class SessioneService implements ISessioneService {

    @Autowired
    private SessioneRepository sessioneRepository;

    @Override
    public Sessione createSession(Utente u) {
        Sessione s = new Sessione();
        s.setUtente(u);
        return sessioneRepository.save(s);
    }

    @Override
    public Utente getUtenteBySessionId(UUID sid) {
        return sessioneRepository.findUtenteBySid(sid);
    }

    @Override
    @Transactional
    public void deleteSession(UUID sid) {
        sessioneRepository.deleteBySid(sid);
    }

    @Override
    public boolean isAdmin(UUID sid) {
        Utente u = getUtenteBySessionId(sid);
        return u != null && u.getRole() == Utente.Role.ADMIN;
    }

    @Override
    public boolean isAdminOrReadonly(UUID sid) {
        Utente u = getUtenteBySessionId(sid);
        return u != null && (u.getRole() == Utente.Role.ADMIN || u.getRole() == Utente.Role.READONLY);
    }
}
