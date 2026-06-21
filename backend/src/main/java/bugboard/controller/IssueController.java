package bugboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import bugboard.dto.AssignIssueRequest;
import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;

import bugboard.exception.NotFoundException;
import bugboard.model.Issue;
import bugboard.service.IIssueCommandService;
import bugboard.service.IIssueQueryService;

import java.util.List;
import java.util.UUID;

/*
 * Espone le rotte HTTP delle issue e basta: il lavoro vero lo fanno i due
 * service, uno per leggere e uno per scrivere. L'id di sessione lo prendo
 * sempre dall'header X-Session-Id e mai dall'URL, così non finisce nei log o
 * nella cronologia del browser. Controlli e permessi stanno nel service; se
 * saltano, ci pensa il GlobalExceptionHandler a tradurli nello status giusto.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private static final String SID_HEADER = "X-Session-Id";

    private final IIssueQueryService queryService;
    private final IIssueCommandService commandService;

    public IssueController(IIssueQueryService queryService, IIssueCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping("/user")
    public List<IssueResponseUser> getIssuesBySessionId(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.findBySessionId(sid);
    }

    @GetMapping("/stakeholder")
    public List<IssueResponse> getIssuesForStakeholder(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.findOnlyBug(sid);
    }

    @GetMapping
    public List<IssueResponse> getAllIssues(@RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.findAllForAdmin(sid);
    }

    @PutMapping("/assign")
    public ResponseEntity<Void> assignIssueToUser(@RequestHeader(value = SID_HEADER, required = false) UUID sid,
                                                  @RequestBody AssignIssueRequest request) {
        commandService.assignIssueToUser(request.getIssueId(), request.getUserEmail(), request.getDataScadenza(), request.getVersion(), sid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IssueResponseUser> createIssue(
            @RequestHeader(value = SID_HEADER, required = false) UUID sid,
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

        IssueResponseUser created = commandService.createIssue(request, sid, immagine);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/immagine")
    public ResponseEntity<byte[]> getImmagineIssue(@PathVariable Integer id) {
        Issue issue = queryService.getIssueById(id);

        if (issue.getImmagine() == null) {
            throw new NotFoundException("Nessuna immagine per la issue " + id);
        }

        String contentType = issue.getImmagineContentType() != null
            ? issue.getImmagineContentType()
            : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(issue.getImmagine());
    }

    @GetMapping("/dettagli/{id}")
    public IssueSpecific getDettagliIssue(@PathVariable int id,
                                          @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.getIssueSpecific(id, sid);
    }

    @GetMapping("/dettagliAdmin/{id}")
    public IssueSpecificAdmin getDettagliIssueAdmin(@PathVariable int id,
                                                    @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.getIssueSpecificAdmin(id, sid);
    }

    @GetMapping("/dettagliStakeholder/{id}")
    public IssueSpecificAdmin getDettagliIssueStakeholder(@PathVariable int id,
                                                          @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        return queryService.getIssueSpecificReadonly(id, sid);
    }

    @PostMapping("/{id}/commento")
    public ResponseEntity<Void> scriviCommento(@PathVariable int id,
                                               @RequestBody String testo,
                                               @RequestParam(required = false) Long version,
                                               @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        commandService.aggiungiCommento(id, testo, version, sid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chiudi/{id}")
    public ResponseEntity<Void> userChiudiIssue(@PathVariable int id,
                                                @RequestParam(required = false) Long version,
                                                @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        commandService.chiudiIssueUtente(id, version, sid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chiudi-admin/{id}")
    public ResponseEntity<Void> adminChiudiIssue(@PathVariable int id,
                                                 @RequestParam(required = false) Long version,
                                                 @RequestHeader(value = SID_HEADER, required = false) UUID sid) {
        commandService.chiudiIssueAdmin(id, version, sid);
        return ResponseEntity.noContent().build();
    }
}
