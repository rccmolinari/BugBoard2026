package bugboard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bugboard.builder.IssueBuilder;
import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
import bugboard.event.IssueAssegnataEvent;
import bugboard.exception.BadRequestException;
import bugboard.exception.ForbiddenException;
import bugboard.exception.NotFoundException;
import bugboard.mapper.IssueMapper;
import bugboard.model.Commento;
import bugboard.model.Issue;
import bugboard.model.Issue.StatoIssue;
import bugboard.model.Utente;
import bugboard.repository.IssueRepository;
import bugboard.repository.UserRepository;

/*
 * Qui dentro vive tutta la logica delle issue: creazione, assegnazione,
 * commenti, chiusura e le varie letture filtrate a seconda del ruolo. Il
 * lavoro di trasformare le entità in DTO lo passo a IssueMapper, così questa
 * classe pensa solo alle regole. Le letture e le scritture stanno in due
 * interfacce diverse, in modo che ognuno si tiri dietro solo i metodi che gli
 * servono. Quando qualcosa non torna lancio una ApiException e allo status
 * HTTP da restituire ci pensa poi il GlobalExceptionHandler.
 */
@Service
public class IssueService implements IIssueQueryService, IIssueCommandService {

    private final IssueRepository issueRepository;
    private final UserRepository utenteRepository;
    private final ISessioneService sessioneService;
    private final IssueMapper issueMapper;
    private final ApplicationEventPublisher eventPublisher;

    public IssueService(IssueRepository issueRepository,
                        UserRepository utenteRepository,
                        ISessioneService sessioneService,
                        IssueMapper issueMapper,
                        ApplicationEventPublisher eventPublisher) {
        this.issueRepository = issueRepository;
        this.utenteRepository = utenteRepository;
        this.sessioneService = sessioneService;
        this.issueMapper = issueMapper;
        this.eventPublisher = eventPublisher;
    }

    // Letture

    @Override
    public List<IssueResponse> findAllForAdmin(UUID sid) {
        requireAdmin(sid);
        return issueRepository.findAllIssuesSenzaImmagine();
    }

    @Override
    public List<IssueResponse> findOnlyBug(UUID sid) {
        requireUser(sid);
        return issueRepository.findOnlyBugWithCreatoreAndDataScadenzaAndDataCreazione()
                .stream()
                .map(issueMapper::toIssueResponse)
                .toList();
    }

    @Override
    public List<IssueResponseUser> findBySessionId(UUID sid) {
        Utente user = requireUser(sid);
        return issueRepository.findByAssegnatoAId(user.getId())
                .stream()
                .map(issueMapper::toIssueResponseUser)
                .toList();
    }

    @Override
    public Issue getIssueById(int id) {
        return findIssueOr404(id);
    }

    @Override
    public IssueSpecific getIssueSpecific(int id, UUID sid) {
        requireUser(sid);
        return issueMapper.toIssueSpecific(findIssueOr404(id));
    }

    @Override
    public IssueSpecificAdmin getIssueSpecificAdmin(int id, UUID sid) {
        requireAdmin(sid);
        return issueMapper.toIssueSpecificAdmin(findIssueOr404(id));
    }

    @Override
    public IssueSpecificAdmin getIssueSpecificReadonly(int id, UUID sid) {
        if (!sessioneService.isAdminOrReadonly(sid)) {
            throw new ForbiddenException("Permessi insufficienti");
        }
        return issueMapper.toIssueSpecificAdmin(findIssueOr404(id));
    }

    // Scritture

    // La issue la monto con IssueBuilder, che si prende carico delle parti
    // un po' noiose: conversioni stringa→enum, lettura dei byte dell'immagine
    // e valori di default.
    @Override
    @Transactional
    public IssueResponseUser createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile) {
        Utente creatore = requireUser(sid);

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

        return issueMapper.toIssueResponseUser(issueRepository.save(nuovaIssue));
    }

    @Override
    @Transactional
    public void assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, UUID adminSID) {
        Utente admin = requireAdmin(adminSID);

        Issue issue = findIssueOr404(issueId);
        Utente destinatario = utenteRepository.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException("Utente destinatario non trovato: " + userEmail));

        if (issue.getStato() == StatoIssue.CLOSED) {
            throw new BadRequestException("Impossibile assegnare una issue chiusa");
        }

        if (expiringDate != null) {
            issue.setDataScadenza(expiringDate.atTime(23, 59, 59));
        }

        issue.setAssegnatoA(destinatario);
        issue.setAssegnatario(admin);
        issueRepository.save(issue);
        eventPublisher.publishEvent(new IssueAssegnataEvent(this, issue, destinatario, admin));
    }

    @Override
    @Transactional
    public void aggiungiCommento(int idIssue, String testo, UUID sid) {
        Utente utente = requireUser(sid);

        if (testo == null || testo.trim().isEmpty()) {
            throw new BadRequestException("Il commento non può essere vuoto");
        }

        Issue issue = findIssueOr404(idIssue);
        if (issue.getCommento() == null) {
            issue.setCommento(new ArrayList<>());
        }

        issue.getCommento().add(new Commento(testo.trim(), LocalDateTime.now(), utente.getEmail()));
        issueRepository.save(issue);
    }

    @Override
    @Transactional
    public void chiudiIssueUtente(int idIssue, UUID sid) {
        Utente utente = requireUser(sid);
        Issue issue = findIssueOr404(idIssue);

        if (issue.getAssegnatoA() == null || !issue.getAssegnatoA().getId().equals(utente.getId())) {
            throw new ForbiddenException("La issue non è assegnata a questo utente");
        }
        if (issue.getStato() == StatoIssue.EXPIRED
                || issue.getStato() == StatoIssue.DONE
                || issue.getStato() == StatoIssue.CLOSED) {
            throw new BadRequestException("La issue non può essere chiusa nello stato attuale");
        }

        issue.setStato(StatoIssue.DONE);
        issueRepository.save(issue);
    }

    @Override
    @Transactional
    public void chiudiIssueAdmin(int idIssue, UUID sid) {
        requireAdmin(sid);
        Issue issue = findIssueOr404(idIssue);

        if (issue.getStato() == StatoIssue.CLOSED) {
            throw new BadRequestException("La issue è già chiusa");
        }

        issue.setStato(StatoIssue.CLOSED);
        issueRepository.save(issue);
    }

    // Metodi di appoggio

    // Pretende una sessione valida e ti ridà l'utente che c'è dietro
    private Utente requireUser(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null) {
            throw new ForbiddenException("Sessione non valida");
        }
        return user;
    }

    // Come requireUser, ma in più pretende che l'utente sia un admin
    private Utente requireAdmin(UUID sid) {
        Utente user = requireUser(sid);
        if (user.getRole() != Utente.Role.ADMIN) {
            throw new ForbiddenException("Permessi insufficienti");
        }
        return user;
    }

    private Issue findIssueOr404(int id) {
        return issueRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Issue non trovata: " + id));
    }
}
