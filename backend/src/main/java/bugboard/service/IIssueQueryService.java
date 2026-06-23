package bugboard.service;

import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
import bugboard.dto.IssueVersion;
import bugboard.model.Issue;

import java.util.List;
import java.util.UUID;

/*
 * Tutte le letture delle issue stanno qui. Chi deve solo leggere (per
 * esempio il controller delle notifiche) si aggancia a questa e non si porta
 * dietro i metodi che modificano i dati.
 */
public interface IIssueQueryService {

    List<IssueResponse> findAllForAdmin(UUID sid);

    // Polling admin leggero: solo id+version di tutte le issue. Il client le
    // confronta con quelle che ha e poi richiede le righe cambiate con getIssuesRows.
    List<IssueVersion> getIssuesVersions(UUID sid);

    // Le righe complete della lista admin per gli id indicati.
    List<IssueResponse> getIssuesRows(List<Integer> ids, UUID sid);

    List<IssueResponse> findOnlyBug(UUID sid);

    List<IssueResponseUser> findBySessionId(UUID sid);

    Issue getIssueById(int id);

    IssueSpecific getIssueSpecific(int id, UUID sid);

    IssueSpecificAdmin getIssueSpecificAdmin(int id, UUID sid);

    IssueSpecificAdmin getIssueSpecificReadonly(int id, UUID sid);
}
