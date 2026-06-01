package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.model.Utente;
import bugboard.model.Issue;

import bugboard.service.IIssueService;
import bugboard.service.NotifyService;
import bugboard.service.ISessioneService;

import bugboard.dto.Notify;

import java.util.List;
import java.util.UUID;

/*
 * DIP — inietta NotifyService, IIssueService e ISessioneService (astrazioni).
 *        In precedenza iniettava NotifyUIService (classe concreta), rendendo
 *        impossibile sostituire l'implementazione senza modificare il controller.
 */
@RestController
@RequestMapping("/api/notifies")
@CrossOrigin(origins = "*")
public class NotificaController {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private ISessioneService sessioneService;

    @Autowired
    private IIssueService issueService;

    @GetMapping("/number/{sid}")
    public int countIssues(@PathVariable UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return 0;
        return notifyService.contaNotificheNonLette(utente.getId());
    }

    @GetMapping("/list/{sid}")
    public List<Notify> getMieNotifiche(@PathVariable UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return List.of();
        return notifyService.getNotificaPerUtente(utente.getId());
    }

    @PutMapping("/leggi/{id}")
    public boolean leggiNotifica(@PathVariable int id) {
        return notifyService.segnaComeLetta(id);
    }

    @GetMapping("/apri/{id}/{sid}")
    public Issue apriNotifica(@PathVariable int id, @PathVariable UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return null;

        Integer idIssue = notifyService.getIssueDaNotifica(id);
        if (idIssue == null) return null;

        notifyService.segnaComeLetta(id);
        return issueService.getIssueById(idIssue);
    }
}
