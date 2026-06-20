package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

// I dati per assegnare una issue: quale, a chi e con quale scadenza.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignIssueRequest {
    private int issueId;
    private String userEmail;
    private LocalDate dataScadenza;
}
