package bugboard.service;
import java.util.UUID;
import org.springframework.stereotype.Service;
import bugboard.repository.SessioneRepository;
import bugboard.model.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import bugboard.model.Sessione;
@Service
public class SessioneService {

    @Autowired
    private SessioneRepository sessioneRepository;
    public Sessione createSession(Utente u) {
        Sessione s = new Sessione();
        s.setUtente(u);
        return sessioneRepository.save(s);
    }
    public Utente getUtenteBySessionId(UUID sid) {
        return sessioneRepository.findUtenteBySid(sid);
    }

    public void deleteSession(UUID sid) {
        sessioneRepository.deleteBySid(sid);
    }
}
