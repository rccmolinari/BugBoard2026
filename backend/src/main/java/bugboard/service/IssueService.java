package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugboard.model.Issue;
import bugboard.repository.IssueRepository;

import java.util.List;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public Issue saveIssue(Issue issue) {
        // Qui potresti aggiungere logica, tipo impostare la data automatica
        return issueRepository.save(issue);
    }
}