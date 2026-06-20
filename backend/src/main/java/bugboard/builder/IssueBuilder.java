package bugboard.builder;

import bugboard.model.Issue;
import bugboard.model.Utente;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/*
 * Costruisce una Issue un pezzo per volta, con i metodi che si concatenano.
 * L'idea è tenere in un posto unico le cose un po' noiose: la conversione
 * delle stringhe in enum, la lettura dei byte dell'immagine e i valori di
 * default. L'unico modo per avere la issue finita è chiamare build(), così
 * non ci si ritrova mai per le mani un oggetto costruito a metà.
 *
 * Non è un bean di Spring: me lo creo con `new` ogni volta che serve e vive
 * giusto il tempo di tirar su una issue.
 */
public class IssueBuilder {

    private String titolo;
    private String descrizione;
    private Integer priorita;
    private Utente creatore;
    private Issue.TipoIssue tipo;
    private Issue.StatoIssue stato = Issue.StatoIssue.TODO;
    private byte[] immagine;
    private String immagineContentType;
    private String[] etichetta;

    public IssueBuilder titolo(String titolo) {
        this.titolo = titolo;
        return this;
    }

    public IssueBuilder descrizione(String descrizione) {
        this.descrizione = descrizione;
        return this;
    }

    public IssueBuilder priorita(Integer priorita) {
        this.priorita = priorita;
        return this;
    }

    public IssueBuilder creatore(Utente creatore) {
        this.creatore = creatore;
        return this;
    }

    // Prende la stringa così com'è dal DTO e la converte in enum
    public IssueBuilder tipo(String tipoValue) {
        if (tipoValue != null) {
            this.tipo = Issue.TipoIssue.fromValue(tipoValue);
        }
        return this;
    }

    // Stessa cosa per lo stato; se non arriva niente parto da TODO
    public IssueBuilder stato(String statoValue) {
        this.stato = statoValue != null
            ? Issue.StatoIssue.fromValue(statoValue)
            : Issue.StatoIssue.TODO;
        return this;
    }

    // Leggo i byte dal file caricato; se la lettura va male lascio perdere l'immagine
    public IssueBuilder immagine(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                this.immagine = file.getBytes();
                this.immagineContentType = file.getContentType();
            } catch (IOException e) {
                // niente immagine, pazienza: la issue la costruisco lo stesso
            }
        }
        return this;
    }

    public IssueBuilder etichetta(String[] etichetta) {
        if (etichetta != null && etichetta.length > 0) {
            this.etichetta = etichetta;
        }
        return this;
    }

    // Sforna la Issue bell'e pronta da salvare
    public Issue build() {
        Issue issue = new Issue();
        issue.setTitolo(titolo);
        issue.setDescrizione(descrizione);
        issue.setPriorita(priorita);
        issue.setCreatore(creatore);
        issue.setTipo(tipo);
        issue.setStato(stato);
        issue.setImmagine(immagine);
        issue.setImmagineContentType(immagineContentType);
        issue.setEtichetta(etichetta);
        return issue;
    }
}
