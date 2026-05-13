package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import bugboard.model.Issue.TipoIssue;
import bugboard.model.Issue.StatoIssue;



@Data //genera metodi getter setter tostring e equals
@NoArgsConstructor //genera costruttore vuoto
@AllArgsConstructor //genera costruttore con tutti i campi
public class CreateIssueRequest {
    private String titolo;
    private String descrizione;
    private TipoIssue tipo; // "bug" o "feature"
    private Integer priorita;
    private StatoIssue stato; // controlla se va messa enum
    private String creatoreEmail; // Email dell'utente che ha creato l'issue
    
}
