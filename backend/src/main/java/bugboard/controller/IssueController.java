package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.AssignIssueRequest;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;

import bugboard.service.IIssueService;
import bugboard.service.ISessioneService;

import bugboard.model.Utente;
import bugboard.model.Issue;

import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/*
 * DIP — inietta IIssueService e ISessioneService (astrazioni).
 *        Il repository non è più iniettato direttamente: tutta la logica
 *        di accesso ai dati passa attraverso il service layer.
 * SRP — il controller si occupa solo di routing HTTP e validazione sessione
 *        di primo livello; nessuna logica di business.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    @Autowired
    private IIssueService issueService;

    @Autowired
    private ISessioneService sessioneService;

    @GetMapping("/user/{sid}")
    public List<IssueResponseUser> getIssuesBySessionId(@PathVariable UUID sid) {
        return issueService.findBySessionId(sid);
    }

    @GetMapping("/stakeholder/{sid}")
    public List<IssueResponse> getIssuesForStakeholder(@PathVariable UUID sid) {
        return issueService.findOnlyBug(sid);
    }

    @PutMapping("/assign/{sid}")
    public boolean assignIssueToUser(@PathVariable UUID sid, @RequestBody AssignIssueRequest request) {
        return issueService.assignIssueToUser(
            request.getIssueId(),
            request.getUserEmail(),
            request.getDataScadenza(),
            sid
        );
    }

    @PutMapping(value = "/create/{sid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Issue createIssue(
            @PathVariable UUID sid,
            @RequestParam String titolo,
            @RequestParam(required = false) String descrizione,
            @RequestParam String tipo,
            @RequestParam Integer priorita,
            @RequestParam(required = false, defaultValue = "todo") String stato,
            @RequestParam(required = false) MultipartFile immagine,
            @RequestParam(required = false) String[] etichetta) {

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitolo(titolo);
        request.setDescrizione(descrizione);
        request.setTipo(tipo);
        request.setPriorita(priorita);
        request.setStato(stato);
        request.setEtichetta(etichetta);

        return issueService.createIssue(request, sid, immagine);
    }

    /*
     * SRP — il controllo admin e la query ottimizzata risiedono in IssueService.
     *        Il controller non accede più al repository direttamente.
     */
    @GetMapping("/{sid}")
    public List<IssueResponse> getAllIssues(@PathVariable UUID sid) {
        return issueService.findAllForAdmin(sid);
    }

    @GetMapping("/{id}/immagine")
    public org.springframework.http.ResponseEntity<byte[]> getImmagineIssue(@PathVariable Integer id) {
        Issue issue = issueService.getIssueById(id);

        if (issue == null || issue.getImmagine() == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        String contentType = issue.getImmagineContentType() != null
            ? issue.getImmagineContentType()
            : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return org.springframework.http.ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(issue.getImmagine());
    }

    @GetMapping("/dettagli/{id}/{sid}")
    public IssueSpecific getDettagliIssue(@PathVariable Integer id, @PathVariable UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return null;

        Issue issue = issueService.getIssueById(id);
        return issueService.getIssueSpecific(issue);
    }

    @PostMapping("/{id}/commento/{sid}")
    public boolean scriviCommento(@PathVariable Integer id, @RequestBody String testo, @PathVariable UUID sid) {
        return issueService.aggiungiCommento(id, testo, sid);
    }

    @GetMapping("/dettagliAdmin/{id}/{sid}")
    public IssueSpecificAdmin getDettagliIssueAdmin(@PathVariable Integer id, @PathVariable UUID sid) {
        Issue issue = issueService.getIssueById(id);
        return issueService.getIssueSpecificAdmin(issue, sid);
    }

    @GetMapping("/dettagliStakeholder/{id}/{sid}")
    public IssueSpecificAdmin getDettagliIssueStakeholder(@PathVariable Integer id, @PathVariable UUID sid) {
        Issue issue = issueService.getIssueById(id);
        return issueService.getIssueSpecificReadonly(issue, sid);
    }

    @PostMapping("/chiudi/{id}/{sid}")
    public boolean userChiudiIssue(@PathVariable Integer id, @PathVariable UUID sid) {
        return issueService.chiudiIssueUtente(id, sid);
    }

    @PostMapping("/chiudi-admin/{id}/{sid}")
    public boolean adminChiudiIssue(@PathVariable Integer id, @PathVariable UUID sid) {
        return issueService.chiudiIssueAdmin(id, sid);
    }
}
