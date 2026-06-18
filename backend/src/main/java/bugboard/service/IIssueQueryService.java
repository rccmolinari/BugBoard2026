package bugboard.service;

import bugboard.dto.IssueResponse;
import bugboard.dto.IssueResponseUser;
import bugboard.dto.IssueSpecific;
import bugboard.dto.IssueSpecificAdmin;
import bugboard.model.Issue;

import java.util.List;
import java.util.UUID;

/*
 * ISP — lato "lettura" delle issue. I client che devono solo leggere
 * (es. NotificaController) dipendono da questa interfaccia e non vedono
 * i metodi di scrittura di IIssueCommandService.
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
