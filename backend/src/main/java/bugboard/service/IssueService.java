package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Issue;
import bugboard.model.Issue.StatoIssue;
import bugboard.model.Issue.TipoIssue;
import bugboard.model.Utente;

import bugboard.repository.IssueRepository;
import bugboard.repository.UtenteRepository;

import bugboard.dto.CreateIssueRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;
    
    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private SessioneService sessioneService;

    public List<Issue> getAllIssues(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if(user.getRole() == Utente.Role.ADMIN){
            return issueRepository.findAllWithCreatoreAndDataScadenza();
        }
        return null;
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

    public List<Issue> findBySessionId(UUID sid) {
        // 
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null) {
            return Collections.emptyList();
        }
        return issueRepository.findByAssegnatoAId(user.getId());
    }




    @Transactional
    public Issue createIssue(CreateIssueRequest request, UUID sid){
        Utente creatore = sessioneService.getUtenteBySessionId(sid);
        if (creatore == null) {
            return null; // Utente non trovato
        }
        Issue nuovaIssue = new Issue();
        nuovaIssue.setTitolo(request.getTitolo());
        nuovaIssue.setDescrizione(request.getDescrizione());
        if(request.getTipo() != null){
           nuovaIssue.setTipo(Issue.TipoIssue.valueOf(request.getTipo())); 
        }

        if(request.getStato() != null){
           nuovaIssue.setStato(Issue.StatoIssue.valueOf(request.getStato()));
        } else {
            nuovaIssue.setStato(Issue.StatoIssue.TODO);
        }

        nuovaIssue.setPriorita(request.getPriorita());
        nuovaIssue.setCreatore(creatore);

     
        return issueRepository.save(nuovaIssue); 
    }
}
