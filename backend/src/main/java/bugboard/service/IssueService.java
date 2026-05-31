package bugboard.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bugboard.model.Commento;
import bugboard.model.Issue;
import bugboard.model.Issue.*;
import bugboard.model.Utente;


import bugboard.repository.IssueRepository;
import bugboard.repository.UserRepository;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;

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
    private UserRepository utenteRepository;

    @Autowired
    private SessioneService sessioneService;

    public List<IssueResponse> getAllIssues(UUID sid) {
        Utente user = sessioneService.getUtenteBySessionId(sid);
        if (user == null || user.getRole() != Utente.Role.ADMIN) {
            return Collections.emptyList();
        }

        List<Issue> issues = issueRepository.findAllWithCreatoreAndDataScadenza();
        List<IssueResponse> response = new ArrayList<>();

        for (Issue i : issues) {
            response.add(new IssueResponse(
                i.getId(),
                i.getTitolo(),
                i.getTipo() != null ? i.getTipo().toString() : null,
                i.getPriorita(),
                i.getStato() != null ? i.getStato().toString() : null,
                i.getCreatore() != null ? i.getCreatore().getEmail() : null,
                i.getAssegnatoA() != null ? i.getAssegnatoA().getEmail() : null,
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

        if (issue == null || utente.isEmpty() || admin == null) return false;

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
        if (user == null) return Collections.emptyList();

        List<Issue> issues = issueRepository.findByAssegnatoAId(user.getId());
        List<IssueResponseUser> response = new ArrayList<>();

        for (Issue i : issues) {
            response.add(new IssueResponseUser(
                i.getId(),
                i.getTitolo(),
                i.getTipo() != null ? i.getTipo().toString() : null,
                i.getPriorita(),
                i.getStato() != null ? i.getStato().toString() : null,
                i.getAssegnatario() != null ? i.getAssegnatario().getEmail() : null,
                i.getDataScadenza(),
                i.getImmagine() != null  // hasImmagine
            ));
        }
        return response;
    }

    @Transactional
    public Issue createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile) {
        Utente creatore = sessioneService.getUtenteBySessionId(sid);
        if (creatore == null) return null;

        Issue nuovaIssue = new Issue();
        nuovaIssue.setTitolo(request.getTitolo());
        nuovaIssue.setDescrizione(request.getDescrizione());
        nuovaIssue.setPriorita(request.getPriorita());
        nuovaIssue.setCreatore(creatore);

        if (request.getTipo() != null) {
            nuovaIssue.setTipo(Issue.TipoIssue.fromValue(request.getTipo()));
        }

        nuovaIssue.setStato(request.getStato() != null
            ? Issue.StatoIssue.fromValue(request.getStato())
            : Issue.StatoIssue.TODO);

        if (immagineFile != null && !immagineFile.isEmpty()) {
            try {
                nuovaIssue.setImmagine(immagineFile.getBytes());
                nuovaIssue.setImmagineContentType(immagineFile.getContentType());
            } catch (IOException e) {
                // immagine ignorata, issue creata comunque
            }
        }

        return issueRepository.save(nuovaIssue);
    }

    public Issue getIssueById(int id) {
        return issueRepository.findById(id).orElse(null);
    }
    

    public IssueSpecific getIssueSpecific(Issue issue) {

        if (issue == null) {
            return null;
        }

        IssueSpecific issueSpecific = new IssueSpecific();
        issueSpecific.setId(issue.getId());
        issueSpecific.setTitolo(issue.getTitolo());
        issueSpecific.setDescrizione(issue.getDescrizione());
        issueSpecific.setImmagine(issue.getImmagine());
        issueSpecific.setImmagineContentType(issue.getImmagineContentType());
        issueSpecific.setPriorita(issue.getPriorita());
        issueSpecific.setEtichetta(issue.getEtichetta());
        issueSpecific.setCommento(issue.getCommento());
        issueSpecific.setDataScadenza(issue.getDataScadenza());
    
        // prendiamo email invece di intero oggetto utente
        if(issue.getAssegnatario() != null) {
            issueSpecific.setEmailAssegnatario(issue.getAssegnatario().getEmail());
        }
        
        // convertiamo tipo da enum a string
        if(issue.getTipo() != null) {
            issueSpecific.setTipo(issue.getTipo().toString());
        }

        // convertiamo stato da enum a string
        if(issue.getStato() != null) {
            issueSpecific.setStato(issue.getStato().toString());
        }

        return issueSpecific;
    }
    


    public IssueSpecificAdmin getIssueSpecificAdmin(Issue issue) {
        
        if(issue == null) {
            return null;
        }
        
        IssueSpecificAdmin issueSpecific = new IssueSpecificAdmin();
        issueSpecific.setId(issue.getId());
        issueSpecific.setTitolo(issue.getTitolo());
        issueSpecific.setDescrizione(issue.getDescrizione());
        issueSpecific.setPriorita(issue.getPriorita());
        issueSpecific.setDataScadenza(issue.getDataScadenza());
        issueSpecific.setImmagine(issue.getImmagine());
        issueSpecific.setImmagineContentType(issue.getImmagineContentType());
        issueSpecific.setEtichetta(issue.getEtichetta());
        issueSpecific.setCommento(issue.getCommento());

        // convertiamo enum a string 
        if(issue.getTipo() != null) {
            issueSpecific.setTipo(issue.getTipo().toString());
        }

        // convertiamo enum a string
        if(issue.getStato() != null) {
            issueSpecific.setStato(issue.getStato().toString());
        }
        
        // prendiamo solo email dell'assegnatario invece dell'intero oggetto utente
        if(issue.getAssegnatario() != null) {
            issueSpecific.setEmailAssegnatario(issue.getAssegnatario().getEmail());
        }

        // prendiamo solo email dell'utente a cui è stata assegnata la issue invece dell'intero oggetto utente
        if(issue.getAssegnatoA() != null) {
            issueSpecific.setEmailAssegnatoA(issue.getAssegnatoA().getEmail());
        }

        // prendiamo solo email del creatore della issue invece dell'intero oggetto utente
        issueSpecific.setEmailCreatore(issue.getCreatore().getEmail());

        return issueSpecific;
    }
        
    @Transactional
    public boolean aggiungiCommento(int idIssue, String testo, UUID sid) {
        Utente utente = sessioneService.getUtenteBySessionId(sid);
        if (utente == null) return false;

        Issue issue = issueRepository.findById(idIssue).orElse(null);
        if (issue == null || testo == null || testo.trim().isEmpty()) return false;

        if (issue.getCommento() == null) {
            issue.setCommento(new ArrayList<>());
        }

        issue.getCommento().add(new Commento(testo.trim(), LocalDateTime.now(), utente.getEmail()));
        issueRepository.save(issue);
        return true;
    }
    

    @Transactional
    public boolean chiudiIssue(int idIssue) {
        Issue issue = issueRepository.findById(idIssue).orElse(null);

        if(issue != null) {
            issue.setStato(StatoIssue.CLOSED);
            issueRepository.save(issue);
            return true;
        }

        return false;
    }












    
}