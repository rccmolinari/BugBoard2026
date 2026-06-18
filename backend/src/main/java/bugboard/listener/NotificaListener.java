package bugboard.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import bugboard.event.IssueAssegnataEvent;
import bugboard.model.Notifica;
import bugboard.repository.NotificaRepository;

/*
 * OBSERVER — ascolta IssueAssegnataEvent pubblicato da IssueService
 * e persiste la notifica sul DB.
 *
 * IssueService non sa che questo listener esiste: zero accoppiamento
 * tra chi pubblica e chi consuma (OCP).
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
