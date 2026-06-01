package bugboard.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bugboard.builder.IssueBuilder;
import bugboard.event.IssueAssegnataEvent;
import bugboard.mapper.IssueMapper;
import bugboard.model.Commento;
import bugboard.model.Issue;
import bugboard.model.Issue.*;
import bugboard.model.Utente;

import bugboard.repository.IssueRepository;
import bugboard.repository.UserRepository;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/*
 * SRP  — gestisce solo la logica di business delle issue.
 *        Il mapping entità→DTO è delegato a IssueMapper.
 * DIP  — dipende da ISessioneService (astrazione).
 * ISP  — implementa IIssueService che espone solo i metodi
 *        effettivamente usati dai controller.
 */
@Service
public class IssueService implements IIssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository utenteRepository;

    @Autowired
    private ISessioneService sessioneService;

    @Autowired
    private IssueMapper issueMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /*
     * Restituisce tutte le issue in forma di DTO ottimizzato (senza immagini).
     * Unico punto di accesso per il controller admin, evita che il controller
     * inietti il repository direttamente.
     */
    @Override
    public List<IssueResponse> findAllForAdmin(UUID sid) {
        if (!sessioneService.isAdmin(sid)) return Collections.emptyList();
        return issueRepository.findAllIssuesSenzaImmagine();
    }

    @Override
    public List<IssueResponse> findOnlyBug(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null) return Collections.emptyList();

        List<Issue> issues = issueRepository.findOnlyBugWithCreatoreAndDataScadenzaAndDataCreazione();
        List<IssueResponse> response = new ArrayList<>();

        for (Issue i : issues) {
            response.add(new IssueResponse(
                i.getId(),
                i.getTitolo(),
                i.getTipo() != null ? i.getTipo().toString() : null,
                i.getPriorita(),
                i.getStato() != null ? i.getStato().toString() : null,
                i.getCreatore() != null ? i.getCreatore().getEmail() : null,
                i.getAssegnatoA() != null ? i.getAssegnatoA().getEmail() : null,
                i.getDataScadenza(),
                i.getDataCreazione()
            ));
        }
        return response;
    }

    @Override
    public List<IssueResponseUser> findBySessionId(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null) return Collections.emptyList();

        List<Issue> issues = issueRepository.findByAssegnatoAId(user.getId());
        List<IssueResponseUser> response = new ArrayList<>();

        for (Issue i : issues) {
            response.add(new IssueResponseUser(
                i.getId(),
                i.getTitolo(),
                i.getTipo() != null ? i.getTipo().toString() : null,
                i.getPriorita(),
                i.getStato() != null ? i.getStato().toString() : null,
                i.getAssegnatario() != null ? i.getAssegnatario().getEmail() : null,
                i.getDataScadenza(),
                i.getDataCreazione(),
                i.getImmagine() != null
            ));
        }
        return response;
    }

    /*
     * BUILDER — la costruzione dell'entità Issue è delegata a IssueBuilder.
     * Conversioni stringa→enum, lettura dei byte dell'immagine e validazione
     * dei campi opzionali avvengono tutte nell'IssueBuilder, non qui.
     */
    @Override
    @Transactional
    public Issue createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile) {
        Utente creatore = sessioneService.getUtenteBySessionId(sid);
        if (creatore == null) return null;

        Issue nuovaIssue = new IssueBuilder()
            .titolo(request.getTitolo())
            .descrizione(request.getDescrizione())
            .priorita(request.getPriorita())
            .creatore(creatore)
            .tipo(request.getTipo())
            .stato(request.getStato())
            .immagine(immagineFile)
            .etichetta(request.getEtichetta())
            .build();

        return issueRepository.save(nuovaIssue);
    }

    @Override
    @Transactional
    public boolean assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, UUID adminSID) {
        Utente admin = sessioneService.getUtenteBySessionId(adminSID);
        if (admin == null || admin.getRole() != Utente.Role.ADMIN) return false;

        Issue issue = issueRepository.findById(issueId).orElse(null);
        Optional<Utente> utente = utenteRepository.findByEmail(userEmail);

        if (issue == null || utente.isEmpty()) return false;
        if (issue.getStato() == StatoIssue.CLOSED) return false;

        if (expiringDate != null) {
            issue.setDataScadenza(expiringDate.atTime(23, 59, 59));
        }

        issue.setAssegnatoA(utente.get());
        issue.setAssegnatario(admin);
        issueRepository.save(issue);
        eventPublisher.publishEvent(new IssueAssegnataEvent(this, issue, utente.get(), admin));
        return true;
    }

    @Override
    public Issue getIssueById(int id) {
        return issueRepository.findById(id).orElse(null);
    }

    /*
     * SRP — il mapping è delegato a IssueMapper; questo metodo è un
     * semplice punto di passaggio per chi usa IIssueService.
     */
    @Override
    public IssueSpecific getIssueSpecific(Issue issue) {
        return issueMapper.toIssueSpecific(issue);
    }

    @Override
    public IssueSpecificAdmin getIssueSpecificAdmin(Issue issue, UUID sid) {
        if (!sessioneService.isAdmin(sid)) return null;
        return issueMapper.toIssueSpecificAdmin(issue);
    }

    @Override
    public IssueSpecificAdmin getIssueSpecificReadonly(Issue issue, UUID sid) {
        if (!sessioneService.isAdminOrReadonly(sid)) return null;
        return issueMapper.toIssueSpecificAdmin(issue);
    }

    @Override
    @Transactional
    public boolean aggiungiCommento(int idIssue, String testo, UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return false;

        Issue issue = issueRepository.findById(idIssue).orElse(null);
        if (issue == null || testo == null || testo.trim().isEmpty()) return false;

        if (issue.getCommento() == null) {
            issue.setCommento(new ArrayList<>());
        }

        issue.getCommento().add(new Commento(testo.trim(), LocalDateTime.now(), utente.getEmail()));
        issueRepository.save(issue);
        return true;
    }

    @Override
    @Transactional
    public boolean chiudiIssueUtente(int idIssue, UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return false;
        Issue issue = issueRepository.findById(idIssue).orElse(null);
        if (issue == null) return false;
        if (issue.getAssegnatoA() == null || !issue.getAssegnatoA().getId().equals(utente.getId())) return false;
        if (issue.getStato() == StatoIssue.EXPIRED || issue.getStato() == StatoIssue.DONE || issue.getStato() == StatoIssue.CLOSED) return false;
        issue.setStato(StatoIssue.DONE);
        issueRepository.save(issue);
        return true;
    }

    @Override
    @Transactional
    public boolean chiudiIssueAdmin(int idIssue, UUID sid) {
        if (!sessioneService.isAdmin(sid)) return false;
        Issue issue = issueRepository.findById(idIssue).orElse(null);
        if (issue == null) return false;
        if (issue.getStato() == StatoIssue.CLOSED) return false;
        issue.setStato(StatoIssue.CLOSED);
        issueRepository.save(issue);
        return true;
    }
}
