package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import  bugboard.service.IssueService;
import bugboard.model.Issue;
import java.util.List;
/*
 * Controller per la gestione delle issue.
 */
@RestController
@RequestMapping("/api/issues")
public class IssueController {

    // Service con la logica vera di autenticazione/registrazione.
    @Autowired
    private IssueService issueService;

   @GetMapping("/user/{userId}")
    public List<Issue> getIssuesByUserId(@PathVariable int userId) {
        return issueService.findByAssegnatoAId(userId);
    }
}
