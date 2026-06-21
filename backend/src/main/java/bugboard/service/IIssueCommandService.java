package bugboard.service;

import bugboard.dto.CreateIssueRequest;
import bugboard.dto.IssueResponseUser;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/*
 * Qui invece ci sono i metodi che modificano le issue: crearle, assegnarle,
 * commentarle e chiuderle. Li tengo separati dalle letture così chi non deve
 * scrivere niente non se li ritrova tra i piedi.
 */
public interface IIssueCommandService {

    IssueResponseUser createIssue(CreateIssueRequest request, UUID sid, MultipartFile immagineFile);

    void assignIssueToUser(int issueId, String userEmail, LocalDate expiringDate, Long expectedVersion, UUID adminSID);

    void aggiungiCommento(int idIssue, String testo, Long expectedVersion, UUID sid);

    void chiudiIssueUtente(int idIssue, Long expectedVersion, UUID sid);

    void chiudiIssueAdmin(int idIssue, Long expectedVersion, UUID sid);
}
