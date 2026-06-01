package bugboard.service;

import bugboard.model.Sessione;
import bugboard.model.Utente;

import java.util.UUID;

/*
 * DIP — tutte le classi che gestiscono sessioni dipendono da questa astrazione.
 * Permette di sostituire SessioneService con una diversa implementazione
 * (es. basata su Redis, JWT, ecc.) senza toccare nessun consumer.
 */
public interface ISessioneService {
    Sessione createSession(Utente utente);
    Utente getUtenteBySessionId(UUID sid);
    void deleteSession(UUID sid);

    /** Restituisce true se la sessione è valida e l'utente ha ruolo ADMIN. */
    boolean isAdmin(UUID sid);

    /** Restituisce true se la sessione è valida e l'utente ha ruolo ADMIN o READONLY. */
    boolean isAdminOrReadonly(UUID sid);
}
