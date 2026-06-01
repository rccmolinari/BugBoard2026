package bugboard.listener;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private NotificaRepository notificaRepository;

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
