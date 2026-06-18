package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data //genera metodi getter setter tostring e equals
@NoArgsConstructor //genera costruttore vuoto
@AllArgsConstructor //genera costruttore con tutti i campi

public class AssignIssueRequest {
    private int issueId;
    private String userEmail;
    private LocalDate dataScadenza;
}
