package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import bugboard.model.Issue;
import bugboard.repository.IssueRepository;
import bugboard.model.Utente;
import bugboard.repository.UtenteRepository;
import java.util.List;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;

    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    public Issue saveIssue(Issue issue) {
        return issueRepository.save(issue);
    }
    @Transactional
    public boolean assignIssueToUser(int issueId, int userId, int adminid) {
        Issue issue = issueRepository.findById(issueId).orElse(null);
        Utente user = utenteRepository.findById(userId).orElse(null);
        Utente admin = utenteRepository.findById(adminid).orElse(null);
        
        if (issue == null) {
            return false; // Issue non trovato
        }
        if (user == null) {
            return false; // User non trovato
        }
        if (admin == null) {
            return false; // Admin non trovato
        }
        
        issue.setAssegnatoA(user);
        issue.setAssegnatario(admin);
        issueRepository.save(issue);
        return true;
    }
    public List<Issue> getIssuesByCreatoreId(Integer creatoreId) {
        return issueRepository.findByCreatoreId(creatoreId);
    }

    public List<Issue> findByAssegnatoAId(Integer userid) {
        return issueRepository.findByAssegnatoAId(userid);
    }

}