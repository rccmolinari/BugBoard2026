package bugboard.mapper;

import org.springframework.stereotype.Component;

import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
import bugboard.model.Issue;

/*
 * Raccoglie in un posto solo tutte le conversioni dall'entità Issue ai vari
 * DTO. Così se cambia la forma di un DTO vengo a metterci mano qui, e il
 * service delle issue se ne resta tranquillo com'è.
 */
@Component
public class IssueMapper {

    // Versione ridotta per le liste di admin e stakeholder, senza immagine
    public IssueResponse toIssueResponse(Issue issue) {
        if (issue == null) return null;

        return new IssueResponse(
            issue.getId(),
            issue.getTitolo(),
            issue.getTipo() != null ? issue.getTipo().toString() : null,
            issue.getPriorita(),
            issue.getStato() != null ? issue.getStato().toString() : null,
            issue.getCreatore() != null ? issue.getCreatore().getEmail() : null,
            issue.getAssegnatoA() != null ? issue.getAssegnatoA().getEmail() : null,
            issue.getDataScadenza(),
            issue.getDataCreazione(),
            issue.getVersion()
        );
    }

    // Per la dashboard utente: dice se c'è un'immagine ma non si trascina i byte
    public IssueResponseUser toIssueResponseUser(Issue issue) {
        if (issue == null) return null;

        return new IssueResponseUser(
            issue.getId(),
            issue.getTitolo(),
            issue.getTipo() != null ? issue.getTipo().toString() : null,
            issue.getPriorita(),
            issue.getStato() != null ? issue.getStato().toString() : null,
            issue.getAssegnatario() != null ? issue.getAssegnatario().getEmail() : null,
            issue.getDataScadenza(),
            issue.getDataCreazione(),
            issue.getImmagine() != null
        );
    }

    public IssueSpecific toIssueSpecific(Issue issue) {
        if (issue == null) return null;

        IssueSpecific dto = new IssueSpecific();
        dto.setId(issue.getId());
        dto.setVersion(issue.getVersion());
        dto.setTitolo(issue.getTitolo());
        dto.setDescrizione(issue.getDescrizione());
        dto.setImmagine(issue.getImmagine());
        dto.setImmagineContentType(issue.getImmagineContentType());
        dto.setPriorita(issue.getPriorita());
        dto.setEtichetta(issue.getEtichetta());
        dto.setCommento(issue.getCommento());
        dto.setDataScadenza(issue.getDataScadenza());
        dto.setDataCreazione(issue.getDataCreazione());
        if (issue.getTipo() != null)        dto.setTipo(issue.getTipo().toString());
        if (issue.getStato() != null)       dto.setStato(issue.getStato().toString());
        if (issue.getAssegnatario() != null) dto.setEmailAssegnatario(issue.getAssegnatario().getEmail());
        return dto;
    }

    public IssueSpecificAdmin toIssueSpecificAdmin(Issue issue) {
        if (issue == null) return null;

        IssueSpecificAdmin dto = new IssueSpecificAdmin();
        dto.setId(issue.getId());
        dto.setVersion(issue.getVersion());
        dto.setTitolo(issue.getTitolo());
        dto.setDescrizione(issue.getDescrizione());
        dto.setPriorita(issue.getPriorita());
        dto.setDataScadenza(issue.getDataScadenza());
        dto.setImmagine(issue.getImmagine());
        dto.setImmagineContentType(issue.getImmagineContentType());
        dto.setEtichetta(issue.getEtichetta());
        dto.setCommento(issue.getCommento());
        if (issue.getTipo() != null)        dto.setTipo(issue.getTipo().toString());
        if (issue.getStato() != null)       dto.setStato(issue.getStato().toString());
        if (issue.getAssegnatario() != null) dto.setEmailAssegnatario(issue.getAssegnatario().getEmail());
        if (issue.getAssegnatoA() != null)  dto.setEmailAssegnatoA(issue.getAssegnatoA().getEmail());
        if (issue.getCreatore() != null)    dto.setEmailCreatore(issue.getCreatore().getEmail());
        return dto;
    }
}
