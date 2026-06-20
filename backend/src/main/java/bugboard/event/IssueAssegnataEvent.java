package bugboard.event;

import bugboard.model.Issue;
import bugboard.model.Utente;
import org.springframework.context.ApplicationEvent;

/*
 * Lo "scatto" che IssueService lancia quando una issue viene assegnata. Si
 * porta dietro tutto quello che serve a chi lo ascolta, ma chi lo lancia non
 * sa né gli importa chi poi lo riceverà.
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
