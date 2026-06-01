package bugboard.service;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
import bugboard.model.Issue;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/*
 * DIP + ISP — IssueController e NotificaController dipendono da questa
 * astrazione. L'interfaccia espone solo i metodi effettivamente usati
 * dai client (nessun metodo "morto" incluso).
 */
public interface IIssueService {

    List<IssueResponse> findAllForAdmin(UUID sid);

    List<IssueResponse> findOnlyBug(UUID sid);

    List<IssueResponseUser> findBySessionId(UUID sid);

    Issue createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile);

    boolean assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, UUID adminSID);

    Issue getIssueById(int id);

    IssueSpecific getIssueSpecific(Issue issue);

    IssueSpecificAdmin getIssueSpecificAdmin(Issue issue, UUID sid);

    IssueSpecificAdmin getIssueSpecificReadonly(Issue issue, UUID sid);

    boolean aggiungiCommento(int idIssue, String testo, UUID sid);

    boolean chiudiIssueUtente(int idIssue, UUID sid);

    boolean chiudiIssueAdmin(int idIssue, UUID sid);
}
