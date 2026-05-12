package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data //genera metodi getter setter tostring e equals
@NoArgsConstructor //genera costruttore vuoto
@AllArgsConstructor //genera costruttore con tutti i campi

public class AssignIssueRequest {
    private int issueId;
    private int userId;
    private int adminId;

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
    
}
