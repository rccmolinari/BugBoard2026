package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueRequest {
    private String titolo;
    private String descrizione;
    private String tipo;
    private Integer priorita;
    private String stato;
    private String creatoreEmail;
    private String[] etichetta;
}