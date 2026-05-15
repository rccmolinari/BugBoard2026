package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.AssignIssueRequest;

import bugboard.service.IssueService;
import bugboard.service.SessioneService;

import bugboard.model.Utente;
import bugboard.model.Issue;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/create/{sid}")
    public Issue createIssue(@PathVariable UUID sid, @RequestBody CreateIssueRequest request) {
 
       Utente creatore = sessioneService.getUtenteBySessionId(sid);
       if (creatore == null) {
          return  null;
       }

       return issueService.createIssue(request, sid);
    }

    @GetMapping("{sid}")
    public List<IssueResponse> getAllIssues(@PathVariable UUID sid) {
        return issueService.getAllIssues(sid);
    }


          





}
