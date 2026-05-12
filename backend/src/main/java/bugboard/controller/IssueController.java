package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import  bugboard.service.IssueService;
import bugboard.model.Issue;
import java.util.List;
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

   @GetMapping("/user/{SID}")
    public List<Issue> getIssuesByUserId(@PathVariable int SID) {
        return issueService.findByAssegnatoAId(SID);
    }

    @PostMapping("/assign")
    public boolean assignIssueToUser(@RequestBody AssignIssueRequest request) {
        return issueService.assignIssueToUser(request.getIssueId(), request.getUserId(), request.getAdminId());
    }

}
