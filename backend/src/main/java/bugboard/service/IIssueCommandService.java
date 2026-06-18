package bugboard.service;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponseUser;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/*
 * ISP — lato "scrittura" delle issue. Separato dalle letture
 * (IIssueQueryService): un client che non modifica issue non deve
 * dipendere da questi metodi. I fallimenti sono segnalati con
 * eccezioni (ApiException).
 */
public interface IIssueCommandService {

    IssueResponseUser createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile);

    void assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, UUID adminSID);

    void aggiungiCommento(int idIssue, String testo, UUID sid);

    void chiudiIssueUtente(int idIssue, UUID sid);

    void chiudiIssueAdmin(int idIssue, UUID sid);
}
