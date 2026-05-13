package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import bugboard.dto.CreateIssueRequest;
import bugboard.service.IssueService;
import bugboard.service.SessioneService;
import bugboard.model.Utente;
import bugboard.model.Issue;
import java.util.List;
import java.util.UUID;
import bugboard.dto.AssignIssueRequest;
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
    public List<Issue> getIssuesBySessionId(@PathVariable UUID sid) {
        return issueService.findBySessionId(sid);
    }

    @PostMapping("/assign/{sid}")
    public boolean assignIssueToUser(@PathVariable UUID sid, @RequestBody AssignIssueRequest request) {
        Utente admin = sessioneService.getUtenteBySessionId(sid);

        if(admin == null){
            return false;
        }
        
        return issueService.assignIssueToUser(
             request.getIssueId(),
             request.getUserId(),
             admin.getId()    
        );
    }

@PostMapping("/create/{sid}")
public ResponseEntity<Issue> createIssue(@PathVariable UUID sid, @RequestBody CreateIssueRequest request) {
 
    Utente creatore = sessioneService.getUtenteBySessionId(sid);
    if (creatore == null) {
        return  new ResponseEntity<Issue>(HttpStatus.UNAUTHORIZED);
    }

    Issue nuovaIssue = issueService.createIssue(request, sid);
    
    return new ResponseEntity<Issue>(nuovaIssue, HttpStatus.CREATED);

}
          





}
