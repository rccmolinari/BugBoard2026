package bugboard.controller;

import org.springframework.web.bind.annotation.*;

import bugboard.dto.IssueSpecific;
import bugboard.dto.Notify;
import bugboard.exception.ForbiddenException;
import bugboard.exception.NotFoundException;
import bugboard.model.Utente;

import bugboard.service.IIssueQueryService;
import bugboard.service.INotifyService;
import bugboard.service.ISessioneService;

import java.util.List;
import java.util.UUID;

/*
 * DIP + ISP — inietta INotifyService, ISessioneService e IIssueQueryService.
 *        Nota: dipende solo dal ruolo "query" delle issue (gli serve solo
 *        leggere il dettaglio), non dall'intero servizio issue.
 *        La sessione arriva dall'header X-Session-Id.
 */
@RestController
@RequestMapping("/api/notifies")
@CrossOrigin(origins = "*")
public class NotificaController {

    private static final String SID_HEADER = "X-Session-Id";

    private final INotifyService notifyService;
    private final ISessioneService sessioneService;
    private final IIssueQueryService queryService;

    public NotificaController(INotifyService notifyService,
                             ISessioneService sessioneService,
                             IIssueQueryService queryService) {
        this.notifyService = notifyService;
        this.sessioneService = sessioneService;
        this.queryService = queryService;
    }

    @GetMapping("/number")
    public int countIssues(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return notifyService.contaNotificheNonLette(requireUser(sid).getId());
    }

    @GetMapping("/list")
    public List<Notify> getMieNotifiche(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return notifyService.getNotificaPerUtente(requireUser(sid).getId());
    }

    @PutMapping("/leggi/{id}")
    public boolean leggiNotifica(@PathVariable int id) {
        return notifyService.segnaComeLetta(id);
    }

    @GetMapping("/apri/{id}")
    public IssueSpecific apriNotifica(@PathVariable int id,
                                      @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        requireUser(sid);

        Integer idIssue = notifyService.getIssueDaNotifica(id);
        if (idIssue == null) {
            throw new NotFoundException("Nessuna issue associata alla notifica " + id);
        }

        notifyService.segnaComeLetta(id);
        return queryService.getIssueSpecific(idIssue, sid);
    }

    private Utente requireUser(UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) {
            throw new ForbiddenException("Sessione non valida");
        }
        return utente;
    }
}
