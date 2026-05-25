package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponseUser {
    private int id;
    private String titolo;
    private String tipo;
    private Integer priorita;
    private String stato;
    private String assegnatoDa;
    private LocalDateTime dataScadenza;
    private boolean hasImmagine;
}