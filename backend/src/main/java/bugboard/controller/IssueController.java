package bugboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import bugboard.service.IssueService;
import bugboard.model.Issue;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping
    public List<Issue> getAllIssues() {
        return issueService.getAllIssues();
    }

    @PostMapping
    public boolean createIssue(@RequestBody Issue issue) {
        Issue savedIssue = issueService.saveIssue(issue);
        if (savedIssue != null) {
            return true;
        }
        return false;
    }

    @PostMapping("/{issueId}/{userEmail}/{adminEmail}")
    public boolean assignIssueToUser(
        @PathVariable int issueId,
        @PathVariable String userEmail,
        @PathVariable String adminEmail
    ) {
        return issueService.assignIssueToUser(issueId, userEmail, adminEmail);
    }   
}