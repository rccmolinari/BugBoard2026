package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.AssignIssueRequest;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;

import bugboard.service.IssueService;
import bugboard.service.SessioneService;

import bugboard.repository.IssueRepository;

import bugboard.model.Utente;
import bugboard.model.Issue;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
/*
 * Controller per la gestione delle issue.
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    // Service con la logica vera di autenticazione/registrazione.
    @Autowired
    private IssueService issueService;

    @Autowired
    private SessioneService sessioneService;

    @Autowired
    private IssueRepository issueRepository;

   @GetMapping("/user/{sid}")
    public List<IssueResponseUser> getIssuesBySessionId(@PathVariable UUID sid) {
        return issueService.findBySessionId(sid);
    }

    @PutMapping("/assign/{sid}")
    public boolean assignIssueToUser(@PathVariable UUID sid, @RequestBody AssignIssueRequest request) {

        Utente admin = sessioneService.getUtenteBySessionId(sid);
        if (admin == null) return false;

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
            @RequestParam(required = false) MultipartFile immagine) {

        Utente creatore = sessioneService.getUtenteBySessionId(sid);
        if (creatore == null) return null;

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitolo(titolo);
        request.setDescrizione(descrizione);
        request.setTipo(tipo);
        request.setPriorita(priorita);
        request.setStato(stato);

        return issueService.createIssue(request, sid, immagine);
    }
    @GetMapping("{sid}")
    public List<IssueResponse> getAllIssues(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null || user.getRole() != Utente.Role.ADMIN) {
            return Collections.emptyList();
        }

        // Il database restituisce già la lista pulita e ottimizzata dei DTO
        return issueRepository.findAllIssuesSenzaImmagine();
    }
    @GetMapping("/{id}/immagine")
        public org.springframework.http.ResponseEntity<byte[]> getImmagineIssue(@PathVariable Integer id) {
            // 1. Recupera la issue tramite il service (o direttamente dal repository se non hai il metodo nel service)
            // Nota: Assicurati che il tuo issueService abbia un modo per trovare la issue singola per ID
            Issue issue = issueService.getIssueById(id); 
            
            if (issue == null || issue.getImmagine() == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            // 2. Recupera il content type salvato (es. image/png), altrimenti usa un default sicuro
            String contentType = issue.getImmagineContentType();
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // 3. Costruisci la risposta HTTP con i byte dell'immagine e gli header corretti
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(issue.getImmagine());
        }
        
        @GetMapping("/dettagli/{id}/{sid}")
        public IssueSpecific getDettagliIssue(@PathVariable Integer id, @PathVariable UUID sid) {
            Utente utente = sessioneService.getUtenteBySessionId(sid);
            
            if(utente != null) {
                Issue idIssue = issueService.getIssueById(id);

                if(idIssue != null){
                  return issueService.getIssueSpecific(idIssue);  
                } else {
                    return null;
                }
            }

            return null;
        }
         
        @PostMapping("/{id}/commento/{sid}")
        public boolean scriviCommento(@PathVariable Integer id, @RequestBody String testo, @PathVariable UUID sid) {
             return issueService.aggiungiCommento(id, testo, sid);
        }

        @GetMapping("/dettagliAdmin/{id}/{sid}")
        public IssueSpecificAdmin getDettagliIssueAdmin(@PathVariable Integer id, @PathVariable UUID sid) {
               
             Utente utente = sessioneService.getUtenteBySessionId(sid);
             
            if(utente != null && utente.getRole() == Utente.Role.ADMIN) {
                Issue idIssue = issueService.getIssueById(id);

                if(idIssue != null){
                    return issueService.getIssueSpecificAdmin(idIssue);
                } else {
                    return null;
                }
            }

            return null;
        }
          





}
