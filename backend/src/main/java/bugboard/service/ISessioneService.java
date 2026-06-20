package bugboard.service;

import bugboard.model.Sessione;
import bugboard.model.Utente;

import java.util.UUID;

/*
 * Il contratto per la gestione delle sessioni. Chi ne ha bisogno si appoggia
 * a questa interfaccia, così se un domani volessimo cambiare il modo di
 * tenere le sessioni (Redis, JWT, quello che sia) basta scrivere un'altra
 * implementazione senza andare a toccare tutto il resto.
 */
public interface ISessioneService {
    Sessione createSession(Utente utente);
    Utente getUtenteBySessionId(UUID sid);
    void deleteSession(UUID sid);

    // Vero se la sessione è valida e dietro c'è un admin
    boolean isAdmin(UUID sid);

    // Vero se la sessione è valida e dietro c'è un admin oppure un readonly
    boolean isAdminOrReadonly(UUID sid);
}
