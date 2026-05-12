package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import  bugboard.service.IssueService;
import bugboard.model.Issue;
import java.util.List;
import java.util.UUID;
import bugboard.dto.AssignIssueRequest;
/*
 * Controller per la gestione delle issue.
 */
@RestController
@RequestMapping("/api/issues")
public class IssueController {

    // Service con la logica vera di autenticazione/registrazione.
    @Autowired
    private IssueService issueService;

   @GetMapping("/user/{sid}")
    public List<Issue> getIssuesBySessionId(@PathVariable UUID sid) {
        return issueService.findBySessionId(sid);
    }

    @PostMapping("/assign")
    public boolean assignIssueToUser(@RequestBody AssignIssueRequest request) {
        return issueService.assignIssueToUser(request.getIssueId(), request.getUserId(), request.getAdminId());
    }

}
