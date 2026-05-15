package bugboard.service;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugboard.model.Issue;
import bugboard.model.Utente;

import bugboard.repository.IssueRepository;
import bugboard.repository.UtenteRepository;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public List<IssueResponse> getAllIssues(UUID sid) {

        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null) return Collections.emptyList();

        List<Issue> issues = issueRepository.findAllWithCreatoreAndDataScadenza();

        if (user.getRole() != Utente.Role.ADMIN) {
            return Collections.emptyList();
        }

        List<IssueResponse> response = new ArrayList<>();

        for (Issue i : issues) {
            response.add(new IssueResponse(
                i.getId(),
                i.getTitolo(),
                i.getTipo() != null ? i.getTipo().toString() : null,
                i.getPriorita(),
                i.getStato() != null ? i.getStato().toString() : null,
                i.getCreatore() != null
                    ? i.getCreatore().getEmail()
                    : null,
                i.getAssegnatoA() != null
                    ? i.getAssegnatoA().getEmail()
                    : null,
                i.getDataScadenza()
            ));
        }

        return response;
    }

    public Issue saveIssue(Issue issue) {
        return issueRepository.save(issue);
    }

    @Transactional
    public boolean assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, UUID adminSID) {

        Issue issue = issueRepository.findById(issueId).orElse(null);
        Optional<Utente> utente = utenteRepository.findByEmail(userEmail);
        Utente admin = sessioneService.getUtenteBySessionId(adminSID);

        if (issue == null || utente.isEmpty() || admin == null) {
            return false;
        }

        if (expiringDate != null) {
            issue.setDataScadenza(expiringDate.atTime(23, 59, 59));
        }

        issue.setAssegnatoA(utente.get());
        issue.setAssegnatario(admin);

        issueRepository.save(issue);
        return true;
    }

    public List<Issue> getIssuesByCreatoreId(Integer creatoreId) {
        return issueRepository.findByCreatoreId(creatoreId);
    }

    public List<IssueResponseUser> findBySessionId(UUID sid) {

            Utente user = sessioneService.getUtenteBySessionId(sid);

            if (user == null) {
                return Collections.emptyList();
            }

            List<Issue> issues = issueRepository.findByAssegnatoAId(user.getId());
            List<IssueResponseUser> response = new ArrayList<>();
            for (Issue i : issues) {
                response.add(new IssueResponseUser(
                    i.getId(),
                    i.getTitolo(),
                    i.getTipo() != null ? i.getTipo().toString() : null,
                    i.getPriorita(),
                    i.getStato() != null ? i.getStato().toString() : null,
                    i.getAssegnatario() != null
                        ? i.getAssegnatario().getEmail()
                        : null,
                    i.getDataScadenza()
                ));
            }
            return response;
        }



    @Transactional
    public Issue createIssue(CreateIssueRequest request, UUID sid) {
        // 1. Recupero l'utente dalla sessione
        Utente creatore = sessioneService.getUtenteBySessionId(sid);
        if (creatore == null) {
            return null; // O lancia un'eccezione personalizzata
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

        // 2. Conversione Tipo con gestione null
        if (request.getTipo() != null) {
            nuovaIssue.setTipo(Issue.TipoIssue.fromValue(request.getTipo()));
        }

        // 3. Conversione Stato con gestione null (default a TODO se vuoto)
        if (request.getStato() != null) {
            nuovaIssue.setStato(Issue.StatoIssue.fromValue(request.getStato()));
        } else {
            nuovaIssue.setStato(Issue.StatoIssue.TODO);
        }

        // Qui puoi aggiungere altri campi come immagini o etichette se presenti nel DTO
        
        return issueRepository.save(nuovaIssue); 
    }
}
