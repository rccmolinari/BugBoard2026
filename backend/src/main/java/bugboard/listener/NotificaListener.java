package bugboard.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import bugboard.event.IssueAssegnataEvent;
import bugboard.model.Notifica;
import bugboard.repository.NotificaRepository;

/*
 * Sta in ascolto dell'evento di assegnazione e, appena scatta, salva la
 * notifica sul database. IssueService non sa nemmeno che questa classe
 * esista: se domani volessimo fare altro alla stessa assegnazione (mandare
 * una mail, scrivere un log...) basta aggiungere un altro listener.
 */
@Component
public class NotificaListener {

    private final NotificaRepository notificaRepository;

    public NotificaListener(NotificaRepository notificaRepository) {
        this.notificaRepository = notificaRepository;
    }

    @EventListener
    @Transactional
    public void onIssueAssegnata(IssueAssegnataEvent event) {
        Notifica n = new Notifica();
        n.setIssue(event.getIssue());
        n.setAssegnatoA(event.getAssegnatoA());
        n.setAssegnatario(event.getAssegnatario());
        notificaRepository.save(n);
    }
}
