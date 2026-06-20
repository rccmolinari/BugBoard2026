package bugboard.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import bugboard.repository.SessioneRepository;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Utente;
import bugboard.model.Sessione;

/*
 * Tiene insieme le sessioni: le crea al login, ritrova l'utente a partire dal
 * loro id e le cancella al logout. Ci sono anche due scorciatoie comode per
 * sapere al volo se dietro una sessione c'è un admin (o un readonly).
 */
@Service
public class SessioneService implements ISessioneService {

    private final SessioneRepository sessioneRepository;

    public SessioneService(SessioneRepository sessioneRepository) {
        this.sessioneRepository = sessioneRepository;
    }

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
