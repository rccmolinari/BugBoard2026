package bugboard.builder;

import bugboard.model.Issue;
import bugboard.model.Utente;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/*
 * BUILDER — costruisce un'entità Issue passo per passo tramite API fluente.
 *
 * Vantaggi rispetto alla sequenza di setter in IssueService:
 *  - un solo posto dove gestire la conversione stringa→enum (tipo, stato)
 *  - un solo posto dove gestire la lettura dei byte dell'immagine
 *  - impossibile ottenere un Issue parzialmente costruito: build() è
 *    l'unico punto di creazione
 *  - aggiungere un nuovo campo richiede un solo metodo qui, non modifiche
 *    sparse nei service (OCP)
 *
 * Non è un @Component Spring: viene istanziato con `new` ogni volta che
 * si costruisce un'issue (ciclo di vita locale, non singleton).
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

    /** Accetta la stringa raw dal DTO e la converte in enum. */
    public IssueBuilder tipo(String tipoValue) {
        if (tipoValue != null) {
            this.tipo = Issue.TipoIssue.fromValue(tipoValue);
        }
        return this;
    }

    /** Accetta la stringa raw dal DTO; se null usa TODO come default. */
    public IssueBuilder stato(String statoValue) {
        this.stato = statoValue != null
            ? Issue.StatoIssue.fromValue(statoValue)
            : Issue.StatoIssue.TODO;
        return this;
    }

    /** Legge i byte dal MultipartFile; se fallisce l'immagine viene ignorata. */
    public IssueBuilder immagine(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                this.immagine = file.getBytes();
                this.immagineContentType = file.getContentType();
            } catch (IOException e) {
                // immagine ignorata, l'issue viene comunque costruita
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

    /** Produce l'entità Issue pronta per essere persistita. */
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
