package bugboard.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bugboard.model.Issue;
import bugboard.model.Utente;

import bugboard.repository.IssueRepository;
import bugboard.repository.UserRepository;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;

import java.time.LocalDate;
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

    
}