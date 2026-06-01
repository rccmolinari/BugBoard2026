package bugboard.event;

import bugboard.model.Issue;
import bugboard.model.Utente;
import org.springframework.context.ApplicationEvent;

/*
 * OBSERVER — evento pubblicato da IssueService quando un'issue viene assegnata.
 * Trasporta tutti i dati necessari al listener senza che il publisher
 * conosca chi li consuma (zero accoppiamento tra IssueService e NotificaListener).
 */
public class IssueAssegnataEvent extends ApplicationEvent {

    private final Issue issue;
    private final Utente assegnatoA;
    private final Utente assegnatario;

    public IssueAssegnataEvent(Object source, Issue issue, Utente assegnatoA, Utente assegnatario) {
        super(source);
        this.issue       = issue;
        this.assegnatoA  = assegnatoA;
        this.assegnatario = assegnatario;
    }

    public Issue   getIssue()        { return issue; }
    public Utente  getAssegnatoA()   { return assegnatoA; }
    public Utente  getAssegnatario() { return assegnatario; }
}
