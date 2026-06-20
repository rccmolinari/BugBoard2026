package bugboard.service;

import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
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

    List<IssueResponse> findOnlyBug(UUID sid);

    List<IssueResponseUser> findBySessionId(UUID sid);

    Issue getIssueById(int id);

    IssueSpecific getIssueSpecific(int id, UUID sid);

    IssueSpecificAdmin getIssueSpecificAdmin(int id, UUID sid);

    IssueSpecificAdmin getIssueSpecificReadonly(int id, UUID sid);
}
